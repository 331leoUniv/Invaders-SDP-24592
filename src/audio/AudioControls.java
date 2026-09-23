package audio;

import engine.Core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Persists audio settings for background music and sound effects.
 * Saved settings are kept after the game exits.
 */
public class AudioControls {

    private static final Logger logger = Core.getLogger();

    private static final String SETTINGS_FILE_NAME = "audio_settings";

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

        saveSetting(BGM_VOLUME_KEY, Integer.toString(vol));
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

        saveSetting(SFX_VOLUME_KEY, Integer.toString(vol));
    }

    /**
     * Saves the global mute state.
     *
     * @param muted true if audio is muted, otherwise false
     */
    public static void saveMuted(boolean muted) {
        saveSetting(MUTED_KEY, Boolean.toString(muted));
    }

    private static synchronized void saveSetting(String key, String value) {
        File settingsFile;
        try {
            settingsFile = getSettingsFile();
        } catch (UnsupportedEncodingException e) {
            logger.warning("Could not locate audio settings file: " + e.getMessage());
            return;
        }

        Properties settings = new Properties();

        if (settingsFile.exists()) {
            try (InputStream in = new FileInputStream(settingsFile)) {
                settings.load(in);
            } catch (IOException e) {
                logger.warning("Could not read audio settings: " + e.getMessage());
            }
        }

        settings.setProperty(key, value);

        try (OutputStream out = new FileOutputStream(settingsFile)) {
            settings.store(out, "Audio settings");
            logger.info("Saved audio setting " + key + " = " + value);
        } catch (IOException e) {
            logger.warning("Could not save audio settings: " + e.getMessage());
        }
    }

    private static File getSettingsFile() throws UnsupportedEncodingException {
        String jarPath = AudioControls.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();
        jarPath = URLDecoder.decode(jarPath, "UTF-8");

        return new File(new File(jarPath).getParent(), SETTINGS_FILE_NAME);
    }
}
