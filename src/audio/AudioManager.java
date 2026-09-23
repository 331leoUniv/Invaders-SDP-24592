package audio;

import engine.Core;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Provides global audio control for background music and sound effects.
 * This class manages BGM playback, SFX playback, volume levels,
 * and the global mute state.
 */
public class AudioManager {

    /** Application logger. */
    private static Logger logger;

    /** Clip for the current background music. */
    private static Clip bgmClip;

    static {
        initialize();
    }

    /**
     * Initializes the audio system and required internal resources.
     */
    private static void initialize() {
        logger = Core.getLogger();
        bgmClip = null;
        logger.info("Audio system initialized.");
    }

    /**
     * Plays the specified background music.
     * If another BGM is already playing, the current implementation
     * may stop or replace it.
     * Only 16-bit PCM WAV files are supported by this application.
     *
     * @param path the resource path relative to the classpath root
     */
    public static void playBGM(String path) {
        AudioInputStream stream = null;
        Clip newClip = null;
        boolean started = false;

        try {
            if (path == null || path.trim().isEmpty())
                throw new IllegalArgumentException("BGM path must not be empty.");

            URL resource = AudioManager.class.getClassLoader().getResource(path);

            if (resource == null)
                throw new IllegalArgumentException("BGM resource not found: " + path);

            stream = AudioSystem.getAudioInputStream(resource);

            newClip = AudioSystem.getClip();
            newClip.open(stream);

            stopBGM();

            newClip.setFramePosition(0);
            newClip.loop(Clip.LOOP_CONTINUOUSLY);

            bgmClip = newClip;
            started = true;

            logger.info("Playing BGM: " + path);

        } catch (Exception e) {
            logger.warning(
                    "Failed to play BGM: " + path + " / " + e);

        } finally {
            if (!started && newClip != null)
                newClip.close();

            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.warning("Failed to close BGM input stream: " + e);
                }
            }
        }
    }

    /**
     * Stops the currently playing background music.
     */
    public static void stopBGM() {
        if (bgmClip == null) {
            logger.info("No BGM is loaded.");
            return;
        }

        bgmClip.stop();
        bgmClip.close();
        bgmClip = null;

        logger.info("BGM stopped.");
    }

    /**
     * Pauses the currently playing background music.
     */
    public static void pauseBGM() {
        if (bgmClip == null || !bgmClip.isOpen()) {
            logger.warning("Cannot pause BGM: no BGM is loaded.");
            return;
        }

        if (!bgmClip.isRunning()) {
            logger.info("BGM is already paused.");
            return;
        }

        bgmClip.stop();
        logger.info("BGM paused.");

    }

    /**
     * Resumes the previously paused background music.
     */
    public static void resumeBGM() {
        if (bgmClip == null || !bgmClip.isOpen()) {
            logger.warning("Cannot resume BGM: no BGM is loaded.");
            return;
        }

        if (bgmClip.isRunning()) {
            logger.info("BGM is already playing.");
            return;
        }

        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        logger.info("BGM resumed.");

    }

    /**
     * Plays the specified sound effect.
     *
     * @param name the name of the sound effect to play
     * @return the playback ID assigned to the started sound effect
     */
    public static int playSFX(String name) {
        return 0;
    }

    /**
     * Stops the sound effect associated with the specified playback ID.
     * If the given ID does not correspond to a currently playing sound effect,
     * the request is ignored.
     *
     * @param id the playback ID of the sound effect to stop
     */
    public static void stopSFX(int id) {

    }

    /**
     * Sets the background music volume.
     *
     * @param vol the volume level, from 0 to 100
     * @throws IllegalArgumentException if the volume is outside the range 0 to 100
     */
    public static void setBGMVolume(int vol) {
        if (vol < 0 || vol > 100) throw new IllegalArgumentException("Volume must be an integer between 0 and 100.");

    }

    /**
     * Sets the sound effect volume.
     *
     * @param vol the volume level, from 0 to 100
     * @throws IllegalArgumentException if the volume is outside the range 0 to 100
     */
    public static void setSFXVolume(int vol) {
        if (vol < 0 || vol > 100) throw new IllegalArgumentException("Volume must be an integer between 0 and 100.");

    }

    /**
     * Returns the current background music volume.
     *
     * @return the BGM volume level, from 0 to 100
     */
    public static int getBGMVolume() {
        return 50;
    }

    /**
     * Returns the current sound effect volume.
     *
     * @return the SFX volume level, from 0 to 100
     */
    public static int getSFXVolume() {
        return 50;
    }

    /**
     * Enables or disables audio muting.
     *
     * @param muted {@code true} to mute all audio,
     *               {@code false} to restore audio
     */
    public static void setMuted(boolean muted) {

    }

    /**
     * Returns whether audio is currently muted.
     *
     * @return {@code true} if audio is muted,
     *         {@code false} otherwise
     */
    public static boolean isMuted() {
        return false;
    }
}