package achievement;

import event.GameplayEventListener;
import event.InfiniteEnemyDefeatedEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class KillAchievementTracker
        implements GameplayEventListener<InfiniteEnemyDefeatedEvent> {

    private static final Logger LOGGER =
            Logger.getLogger(KillAchievementTracker.class.getName());

    private final Path saveFile;
    private final Map<String, Long> unlockedAt = new LinkedHashMap<>();
    private final List<String> newUnlocks = new ArrayList<>();

    private long runKills;
    private long totalKills;
    private boolean savingEnabled = true;

    public KillAchievementTracker(Path saveFile) {
        if (saveFile == null) {
            throw new IllegalArgumentException("saveFile must not be null");
        }
        this.saveFile = saveFile.toAbsolutePath();

        try {
            load();
        } catch (IOException | IllegalArgumentException exception) {
            
            savingEnabled = false;
            LOGGER.log(Level.SEVERE,
                    "Could not load achievements; original file preserved.",
                    exception);
        }
    }

    public void startRun() {
        runKills = 0;
    }

    @Override
    public void onEvent(InfiniteEnemyDefeatedEvent event) {
        runKills++;
        totalKills++;

        if (runKills % 10 == 0) {
            unlock("INFINITE_RUN_KILLS_" + runKills);
        }
        if (totalKills % 10 == 0) {
            unlock("INFINITE_TOTAL_KILLS_" + totalKills);
        }

        if (savingEnabled) {
            try {
                save();
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE,
                        "Could not save achievements; will retry on next kill.",
                        exception);
            }
        }
    }

    private void unlock(String id) {
        if (unlockedAt.containsKey(id)) {
            return;
        }
        unlockedAt.put(id, System.currentTimeMillis());
        newUnlocks.add(id);
        LOGGER.info("Achievement unlocked: " + id);
    }

    public long getRunKills() {
        return runKills;
    }

    public long getTotalKills() {
        return totalKills;
    }

    public boolean isUnlocked(String id) {
        return unlockedAt.containsKey(id);
    }

    public List<String> drainNewUnlocks() {
        List<String> result = new ArrayList<>(newUnlocks);
        newUnlocks.clear();
        return result;
    }

    private void load() throws IOException {
        if (!Files.exists(saveFile)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(saveFile)) {
            properties.load(input);
        }

        long loadedTotal =
                Long.parseLong(properties.getProperty("totalKills"));
        if (loadedTotal < 0) {
            throw new IOException("Negative totalKills in save file");
        }

        Map<String, Long> loadedUnlocks = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if ("totalKills".equals(key)) {
                continue;
            }
            if (!key.startsWith("unlock.")) {
                throw new IOException("Unknown save entry: " + key);
            }

            String id = key.substring("unlock.".length());
            if (!id.matches("INFINITE_(RUN|TOTAL)_KILLS_[1-9][0-9]*0")) {
                throw new IOException("Invalid achievement ID: " + id);
            }

            long time = Long.parseLong(properties.getProperty(key));
            if (time <= 0) {
                throw new IOException("Invalid unlock time: " + id);
            }
            loadedUnlocks.put(id, time);
        }

        totalKills = loadedTotal;
        unlockedAt.putAll(loadedUnlocks);
    }

    private void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("totalKills", Long.toString(totalKills));
        for (Map.Entry<String, Long> entry : unlockedAt.entrySet()) {
            properties.setProperty(
                    "unlock." + entry.getKey(),
                    Long.toString(entry.getValue()));
        }

        Path parent = saveFile.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(
                parent, "achievements-", ".tmp");

        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "Infinite mode achievements");
            }

            try {
                Files.move(temporary, saveFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, saveFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}