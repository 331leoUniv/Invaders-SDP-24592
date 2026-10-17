package audio;

import engine.Core;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Caches audio formats and sample data by classpath resource path.
 * Resources are loaded on first request and reused for subsequent playback.
 * Cache access is synchronized. Use PCM WAV resources for compatibility.
 */
public class AudioCache {

    /** Application logger. */
    private final Logger logger = Core.getLogger();

    /** Successfully loaded audio resources indexed by their classpath paths. */
    private final Map<String, AudioResource> cacheMap = new HashMap<>();

    /**
     * Returns cached audio data, loading and caching the resource if necessary.
     * Null paths and failed loads are not cached.
     *
     * @param path the resource path relative to the classpath root,
     *             without a leading slash or the res directory prefix
     * @return the cached or newly loaded resource, or null if the path is null
     *         or the resource cannot be loaded
     */
    public synchronized AudioResource getAudioData(String path) {
        if (path == null) return null;
        if (cacheMap.containsKey(path)) return cacheMap.get(path);

        AudioResource data = readAudioData(path);
        if (data != null) cacheMap.put(path, data);

        return data;
    }

    /**
     * Reads the format and sample data from an audio resource.
     * The input stream is closed automatically, and loading failures are logged.
     * This method does not update the cache.
     *
     * @param path the non-null classpath resource path
     * @return the loaded resource, or null if it is missing or reading fails
     */
    private AudioResource readAudioData(String path) {
        URL resource = AudioCache.class.getClassLoader().getResource(path);

        if (resource == null) {
            logger.warning("Audio resource not found: " + path);
            return null;
        }

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(resource)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int frameSize = stream.getFormat().getFrameSize();
            byte[] buffer = new byte[frameSize * 1024];
            int count;

            while ((count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }

            return new AudioResource(stream.getFormat(), output.toByteArray());
        } catch (Exception e) {
            logger.warning("Failed to read audio file: " + path + " / " + e);
        }

        return null;
    }
}
