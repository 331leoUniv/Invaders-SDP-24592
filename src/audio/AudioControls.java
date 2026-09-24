package audio;

import engine.Core;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Persists audio settings for background music and sound effects.
 * Saved settings are kept after the game exits.
 */
public class AudioControls {

    private static final Logger logger = Core.getLogger();

    private static final Preferences prefs = Preferences.userNodeForPackage(AudioControls.class);

    private static final String BGM_VOLUME_KEY = "bgmVolume";

    private static final String SFX_VOLUME_KEY = "sfxVolume";

    private static final String MUTED_KEY = "muted";

    /**
     * Saves the background music volume.
     *
     * @param vol the volume level, from 0 to 100
     */
    public static void saveBGMVolume(int vol) {
        if (vol < 0 || vol > 100) {
            logger.warning("Volume must be an integer between 0 and 100.");
            return;
        }

        prefs.putInt(BGM_VOLUME_KEY, vol);
        logger.info("Saved BGM volume " + vol);
    }

    /**
     * Saves the sound effect volume.
     *
     * @param vol the volume level, from 0 to 100
     */
    public static void saveSFXVolume(int vol) {
        if (vol < 0 || vol > 100) {
            logger.warning("Volume must be an integer between 0 and 100.");
            return;
        }

        prefs.putInt(SFX_VOLUME_KEY, vol);
        logger.info("Saved SFX volume " + vol);
    }

    /**
     * Saves the global mute state.
     *
     * @param muted true if audio is muted, otherwise false
     */
    public static void saveMuted(boolean muted) {
        prefs.putBoolean(MUTED_KEY, muted);
        logger.info("Saved mute state " + muted);
    }
}
