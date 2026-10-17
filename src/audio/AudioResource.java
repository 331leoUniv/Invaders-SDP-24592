package audio;

import javax.sound.sampled.AudioFormat;

/**
 * Holds the audio format and sample data used to open a playback clip.
 * The sample array is shared with the cache and must not be modified.
 */
public class AudioResource {

    /** Format describing the cached audio samples. */
    private final AudioFormat format;

    /** Audio sample bytes, excluding the source file's container headers. */
    private final byte[] data;

    /**
     * Creates an audio resource using the supplied format and sample array.
     * The array is stored directly without copying.
     *
     * @param format the format of the audio samples
     * @param data the audio sample bytes
     */
    AudioResource(AudioFormat format, byte[] data) {
        this.format = format;
        this.data = data;
    }

    /**
     * Returns the format describing the audio samples.
     *
     * @return the audio sample format
     */
    public AudioFormat getFormat() {
        return format;
    }

    public int size() {
        return data.length;
    }

    public byte getData(int index) {
        return data[index];
    }
}
