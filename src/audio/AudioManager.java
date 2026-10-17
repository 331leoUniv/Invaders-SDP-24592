package audio;

import engine.Core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides global audio control for background music and sound effects.
 * This class manages BGM playback, SFX playback, volume levels,
 * and the global mute state.
 */
public class AudioManager {

    /** Application logger. */
    private static final Logger logger = Core.getLogger();

    /** Shared audio resource cache for background music and sound effects. */
    private static AudioCache audioCache;

    private static AudioMixer audioMixer;

    private static final AudioPlaybackGroup bgmGroup = new AudioPlaybackGroup(true);

    private static final AudioPlaybackGroup sfxGroup = new AudioPlaybackGroup();;

    private static boolean initialized = false;

    /** Global mute state, visible across audio control threads. */
    private static volatile boolean muted = false;

    static {
        initialize();
    }

    /**
     * Initializes the audio system and required internal resources.
     */
    private static void initialize() {
        if (initialized) return;
        try {
            audioMixer = new AudioMixer();
            audioMixer.registerPlaybackGroup("bgm", bgmGroup);
            audioMixer.registerPlaybackGroup("sfx", sfxGroup);
            audioCache = new AudioCache();
            logger.info("Audio system initialized.");
            initialized = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize audio system: " + e, e);
        }
    }

    /**
     * Plays the specified background music in a continuous loop.
     * Playback failures are logged.
     * Use 16-bit PCM WAV resources for compatibility.
     *
     * @param path the resource path relative to the classpath root,
     *             without a leading slash or the res directory prefix
     */
    public static void playBGM(String path) {
        if (!initialized) initialize();
        try {
            AudioResource resource = audioCache.getAudioData(path);
            AudioPlaybackGroup group = bgmGroup;
            synchronized (group) {
                group.removeAllPlaybacks();
                group.createPlayback(resource);
                group.setPaused(false);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to play BGM [path=" + path + "]: " + e, e);
        }
    }

    /**
     * Stops the currently playing background music.
     */
    public static void stopBGM() {
        try {
            bgmGroup.removeAllPlaybacks();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to stop BGM: " + e, e);
        }
    }

    /**
     * Pauses the currently playing background music.
     */
    public static void pauseBGM() {
        try {
            bgmGroup.setPaused(true);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to pause BGM: " + e, e);
        }
    }

    /**
     * Resumes the previously paused background music.
     */
    public static void resumeBGM() {
        try {
            bgmGroup.setPaused(false);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to resume BGM: " + e, e);
        }
    }

    /**
     * Plays the specified sound effect once.
     * Multiple sound effects can play simultaneously.
     * Each playback receives a separate ID.
     * Finished clips are automatically removed and closed.
     * Playback failures are logged.
     * Use 16-bit PCM WAV resources for compatibility.
     *
     * @param path the resource path relative to the classpath root,
     *             without a leading slash or the res directory prefix
     * @return the playback ID, or -1 if playback fails
     */
    public static int playSFX(String path) {
        if (!initialized) initialize();
        try {
            return sfxGroup.createPlayback(audioCache.getAudioData(path));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to play SFX [path=" + path + "]: " + e, e);
        }
        return -1;
    }

    /**
     * Stops the specified sound effect and releases its audio resources.
     * If the ID is unknown or already completed, logs a warning
     * and returns without stopping any other sound effect.
     *
     * @param id the playback ID returned by playSFX
     */
    public static void stopSFX(int id) {
        try {
            sfxGroup.removePlayback(id);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to stop SFX [id=" + id + "]: " + e, e);
        }
    }

    /**
     * Sets the background music volume.
     * Invalid volume values are logged and ignored.
     *
     * @param vol the volume level, from 0 to 100
     */
    public static void setBGMVolume(int vol) {
        try {
            bgmGroup.setVolume(vol);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to set BGM volume [volume=" + vol + "]: " + e, e);
        }
    }

    /**
     * Sets the sound effect volume.
     * Invalid volume values are logged and ignored.
     *
     * @param vol the volume level, from 0 to 100
     */
    public static void setSFXVolume(int vol) {
        try {
            sfxGroup.setVolume(vol);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to set SFX volume [volume=" + vol + "]: " + e, e);
        }
    }

    /**
     * Returns the current background music volume.
     *
     * @return the BGM volume level, from 0 to 100
     */
    public static int getBGMVolume() {
        try {
            return bgmGroup.getVolume();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get BGM volume; returning default 50: " + e, e);
        }
        return 50;
    }

    /**
     * Returns the current sound effect volume.
     *
     * @return the SFX volume level, from 0 to 100
     */
    public static int getSFXVolume() {
        try {
            return sfxGroup.getVolume();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get SFX volume; returning default 50: " + e, e);
        }
        return 50;
    }

    /**
     * Updates the global mute state without changing stored volume levels.
     *
     * @param value true to mute audio, false to restore audio
     */
    public static synchronized void setMuted(boolean value) {
        try {
            bgmGroup.setMuted(value);
            sfxGroup.setMuted(value);
            muted = value;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to set global mute [muted=" + value + "]: " + e, e);
        }
    }

    /**
     * Returns the global mute setting.
     *
     * @return true if muting is enabled, otherwise false
     */
    public static synchronized boolean isMuted() {
        return muted;
    }
}
