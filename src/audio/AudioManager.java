package audio;

/**
 * Provides global audio control for background music and sound effects.
 * This class manages BGM playback, SFX playback, volume levels,
 * and the global mute state.
 */
public class AudioManager {

    static {
        initialize();
    }

    /**
     * Initializes the audio system and required internal resources.
     */
    private static void initialize() {

    }

    /**
     * Plays the specified background music.
     * If another BGM is already playing, the current implementation
     * may stop or replace it.
     *
     * @param name the name of the background music to play
     */
    public static void playBGM(String name) {

    }

    /**
     * Stops the currently playing background music.
     */
    public static void stopBGM() {

    }

    /**
     * Pauses the currently playing background music.
     */
    public static void pauseBGM() {

    }

    /**
     * Resumes the previously paused background music.
     */
    public static void resumeBGM() {

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