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

    private final Map<String, AudioPlaybackGroup> playbackGroups = new HashMap<>();

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
        Queue<Integer> finishedPlaybackIds = new LinkedList<>();
        for (int offset = 0; offset < buffer.length; offset += FORMAT.getFrameSize()) {
            double left = 0.0;
            double right = 0.0;
            for (AudioPlaybackGroup group : playbackGroups.values()) {
                synchronized (group) {
                    for (AudioPlayback playback : group.getAllPlaybacks()) {
                        if (!group.isMuted() && !group.isPaused()) {
                            left += playback.getLeftSample() * (group.getVolume() / 50.0);
                            right += playback.getRightSample() * (group.getVolume() / 50.0);
                        }
                        if (!group.isPaused()) {
                            playback.advance();
                            if (playback.isFinished()) finishedPlaybackIds.add(playback.getId());
                        }
                    }

                    while(!finishedPlaybackIds.isEmpty()) {
                        group.removePlayback(finishedPlaybackIds.poll());
                    }
                }
            }
            writeSample(buffer, offset, left);
            writeSample(buffer, offset+2, right);
        }
    }

    private static void writeSample(byte[] output, int offset, double value) {
        int sample = (int) Math.max(-32768, Math.min(32767, Math.round(value)));
        output[offset] = (byte) sample;
        output[offset + 1] = (byte) (sample >> 8);
    }

    public synchronized void registerPlaybackGroup(String key, AudioPlaybackGroup group) {
        if (group == null) throw new IllegalArgumentException("Group cannot be null");
        playbackGroups.put(key, group);
    }

    public synchronized void unregisterPlaybackGroup(String key) {
        AudioPlaybackGroup group = playbackGroups.remove(key);
        if (group == null) throw new IllegalArgumentException("No playback group found for key : " + key);
    }

    public synchronized AudioPlaybackGroup getPlaybackGroup(String key) {
        AudioPlaybackGroup group = playbackGroups.get(key);
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

class AudioPlaybackGroup {

    private Map<Integer, AudioPlayback> playbacks = new HashMap<>();

    private int volume = 50;

    private boolean paused = false;

    private boolean muted = false;

    private boolean looping;

    private int nextId = 0;

    public AudioPlaybackGroup(boolean looping) {
        this.looping = looping;
    }

    public AudioPlaybackGroup() {
        this(false);
    }

    public synchronized int createPlayback(AudioResource resource) {
        int id = nextId++;
        playbacks.put(id, new AudioPlayback(id, this, resource));

        return id;
    }

    public synchronized void removePlayback(int id) {
        AudioPlayback playback = playbacks.remove(id);
        if (playback == null)
            throw new IllegalArgumentException("No active playback found for ID: " + id);
    }

    public synchronized void removeAllPlaybacks() {
        playbacks.clear();
    }

    public synchronized Iterable<AudioPlayback> getAllPlaybacks() {
        return playbacks.values();
    }

    public synchronized void setVolume(int volume) {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100: " + volume);
        }
        this.volume = volume;
    }

    public synchronized int getVolume() {
        return volume;
    }

    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void setMuted(boolean muted) {
        this.muted = muted;
    }

    public synchronized boolean isMuted() {
        return muted;
    }

    public synchronized void setLooping(boolean looping) {
        this.looping = looping;
    }

    public synchronized boolean isLooping() {
        return looping;
    }

}

class AudioPlayback {
    private int id;
    private AudioPlaybackGroup group;
    private final byte[] data;
    private int position = 0;
    private boolean finished = false;

    public AudioPlayback(int id, AudioPlaybackGroup group, AudioResource resource) {
        if (!AudioMixer.FORMAT.matches(resource.getFormat()))
            throw new IllegalArgumentException("Unexpected audio format: " + resource.getFormat());

        this.id = id;
        this.group = group;
        data = resource.getData();
        if (data.length == 0 || data.length % AudioMixer.FORMAT.getFrameSize() != 0)
            throw new IllegalArgumentException("Audio must contain complete stereo PCM frames");
    }

    public double getLeftSample() {
        if (position >= data.length)
            return 0.0;

        return (short) ((data[position] & 0xff) | (data[position + 1] << 8));
    }

    public double getRightSample() {
        if (position >= data.length)
            return 0.0;

        return (short) ((data[position + 2] & 0xff) | (data[position + 3] << 8));
    }

    public void advance() {
        if (position < data.length)
            position += AudioMixer.FORMAT.getFrameSize();
        if (position == data.length) {
            if (group.isLooping()) position = 0;
            else finished = true;
        }
    }

    public int getId() {
        return id;
    }

    public boolean isFinished() {
        return finished;
    }
}
