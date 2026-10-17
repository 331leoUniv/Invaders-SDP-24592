package audio;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class AudioBus {
    private Map<Integer, AudioPlayback> playbacks = new ConcurrentHashMap<>();

    private int volume = 50;

    private boolean paused = false;

    private boolean muted = false;

    private boolean looping;

    private int nextId = 0;

    public AudioBus(boolean looping) {
        this.looping = looping;
    }

    public AudioBus() {
        this(false);
    }

    public synchronized int play(AudioResource resource) {
        int id = nextId++;
        playbacks.put(id, new AudioPlayback(resource));

        return id;
    }

    public synchronized void stop(int id) {
        AudioPlayback playback = playbacks.remove(id);
        if (playback == null)
            throw new IllegalArgumentException("No active playback found for ID: " + id);
    }

    public synchronized void stopAll() {
        playbacks.clear();
    }

    public synchronized void collectSamples(Queue<Double> left, Queue<Double> right) {
        for (Map.Entry<Integer, AudioPlayback> entry : playbacks.entrySet()) {
            AudioPlayback playback = entry.getValue();
            if (!muted && !paused) {
                left.offer(playback.getLeftSample());
                right.offer(playback.getRightSample());
            }
            if (!paused) playback.advance(looping);
            if (playback.isFinished()) playbacks.remove(entry.getKey());
        }
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

    private static class AudioPlayback {
        private AudioResource resource;
        private int position = 0;

        public AudioPlayback(AudioResource resource) {
            if (!AudioMixer.FORMAT.matches(resource.getFormat()))
                throw new IllegalArgumentException("Unexpected audio format: " + resource.getFormat());

            if (resource.size() == 0 || resource.size() % AudioMixer.FORMAT.getFrameSize() != 0)
                throw new IllegalArgumentException("Audio must contain complete stereo PCM frames");

            this.resource = resource;
        }

        public double getLeftSample() {
            if (position >= resource.size())
                return 0.0;

            return (short) ((resource.getData(position) & 0xff) | (resource.getData(position + 1) << 8));
        }

        public double getRightSample() {
            if (position >= resource.size())
                return 0.0;

            return (short) ((resource.getData(position + 2) & 0xff) | (resource.getData(position + 3) << 8));
        }

        public void advance(boolean loop) {
            if (position < resource.size())
                position += AudioMixer.FORMAT.getFrameSize();
            if (position == resource.size()) {
                if (loop) position = 0;
            }
        }

        public boolean isFinished() {
            return position == resource.size();
        }
    }
}
