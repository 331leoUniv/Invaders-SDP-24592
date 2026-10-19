package audio;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Loads every current WAV through the real cache and registers it for bus playback.
 * No audio device is opened and no source file is modified.
 */
class AudioAssetsPlaybackTest {
    /**
     * Creates one playback compatibility test for each WAV under {@code res/audio}.
     *
     * @return Tests named after their classpath resource paths.
     * @throws IOException If the audio asset directory cannot be read.
     */
    @TestFactory
    List<DynamicTest> allWavAssetsLoadAndPlay() throws IOException {
        Path resources = Paths.get("res");
        Path audio = resources.resolve("audio");
        assertTrue(Files.isDirectory(audio), "Run the tests from the project root: " + audio);

        List<Path> files;
        try (Stream<Path> paths = Files.walk(audio)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT).endsWith(".wav"))
                    .sorted()
                    .collect(Collectors.toList());
        }
        assertFalse(files.isEmpty(), "No WAV assets found under " + audio);

        AudioCache cache = new AudioCache();
        List<DynamicTest> tests = new ArrayList<>();
        for (Path file : files) {
            String resourcePath = resources.relativize(file).toString()
                    .replace(File.separatorChar, '/');
            tests.add(DynamicTest.dynamicTest(resourcePath,
                    () -> checkPlayback(cache, resourcePath)));
        }
        return tests;
    }

    /**
     * Loads and converts an asset through the cache, registers it with a bus,
     * and checks that one stereo frame can be collected. Format errors from
     * {@link AudioBus#play(AudioResource)} fail the test. Playback is cleared
     * after each check.
     *
     * @param cache Cache used to load the audio asset.
     * @param path Classpath resource path of the WAV asset.
     */
    private static void checkPlayback(AudioCache cache, String path) {
        AudioBus bus = new AudioBus(path.startsWith("audio/bgm/"));
        try {
            AudioResource resource = cache.getAudioData(path);
            assertNotNull(resource, "Cache failed to load: " + path);

            int playbackId = bus.play(resource);
            assertTrue(playbackId >= 0, "Bus did not register playback: " + path);

            Queue<Double> left = new ArrayDeque<>();
            Queue<Double> right = new ArrayDeque<>();
            bus.collectSamples(left, right);
            assertEquals(1, left.size(), "No left-channel frame: " + path);
            assertEquals(1, right.size(), "No right-channel frame: " + path);
        } finally {
            bus.stopAll();
        }
    }
}
