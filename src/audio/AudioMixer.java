package audio;

import engine.Core;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.*;
import java.util.logging.Logger;

public class AudioMixer implements Runnable {

    public static final AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, false);

    private static final int CHUNK_FRAMES = 441;

    private static final int BUFFER_FRAMES = CHUNK_FRAMES * 4;

    public final Logger logger;

    private final Map<String, AudioBus> audioBuses = new HashMap<>();

    private final Queue<Double> leftBuffer = new ArrayDeque<>();

    private final Queue<Double> rightBuffer = new ArrayDeque<>();

    private boolean alive = false;

    private final SourceDataLine line;

    public AudioMixer() throws LineUnavailableException {
        logger = Core.getLogger();

        line = AudioSystem.getSourceDataLine(FORMAT);
        line.open(FORMAT, BUFFER_FRAMES * FORMAT.getFrameSize());
        line.start();

        alive = true;
        try {
            Thread worker = new Thread(this);
            worker.setDaemon(true);
            worker.start();
        } catch (Exception e) {
            alive = false;
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            byte[] chunk = new byte[CHUNK_FRAMES * FORMAT.getFrameSize()];
            while (isAlive() && line.isOpen()) {
                mix(chunk);
                int offset = 0;
                while (offset < chunk.length) {
                    int written = line.write(chunk, offset, chunk.length - offset);
                    if (written <= 0) throw new IllegalStateException("Audio output stopped");
                    offset += written;
                }
            }
        } catch (Exception e) {
            logger.severe("Audio mixer stopped due to an unexpected error: " + e);
        } finally {
            shutdown();
        }
    }

    private synchronized void mix(byte[] buffer) {
        for (int offset = 0; offset < buffer.length; offset += FORMAT.getFrameSize()) {
            double left = 0.0;
            double right = 0.0;
            for (AudioBus bus : audioBuses.values()) {
                double tLeft = 0.0;
                double tRight = 0.0;
                bus.collectSamples(leftBuffer, rightBuffer);
                while (!leftBuffer.isEmpty() && !rightBuffer.isEmpty()) {
                    tLeft += leftBuffer.poll();
                    tRight += rightBuffer.poll();
                }
                left += tLeft * (bus.getVolume() / 50.0);
                right += tRight * (bus.getVolume() / 50.0);
            }
            writeSample(buffer, offset, left);
            writeSample(buffer, offset + 2, right);
        }
    }

    private static void writeSample(byte[] output, int offset, double value) {
        int sample = (int) Math.max(-32768, Math.min(32767, Math.round(value)));
        output[offset] = (byte) sample;
        output[offset + 1] = (byte) (sample >> 8);
    }

    public synchronized void registerPlaybackGroup(String key, AudioBus bus) {
        if (bus == null) throw new IllegalArgumentException("Group cannot be null");
        audioBuses.put(key, bus);
    }

    public synchronized void unregisterPlaybackGroup(String key) {
        AudioBus group = audioBuses.remove(key);
        if (group == null) throw new IllegalArgumentException("No playback group found for key : " + key);
    }

    public synchronized AudioBus getPlaybackGroup(String key) {
        AudioBus group = audioBuses.get(key);
        if (group == null) throw new IllegalArgumentException("No playback group found for key : " + key);

        return group;
    }

    public synchronized boolean isAlive() {
        return alive;
    }

    public synchronized void shutdown() {
        alive = false;
    }
}
