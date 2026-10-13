package records;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
public final class RecordsSmokeTest {
    private static int checks;
    private interface CheckedAction { void run(); }
    public static void main(final String[] args) throws Exception {
        testRunRecordValidation();
        testRepositoryDeduplication();
        testRunSessionTimingAndFinish();
        testFormatterOutput();
        testPersonalBests();
        testRankingOrderAndLimit();
        System.out.println("RecordsSmokeTest passed: "+checks+" checks.");
    }
private static void testRunRecordValidation() {
		RunRecord record = new RunRecord("run-a", -100, 4, 2500, 123456);
		assertEquals("run-a", record.getRunId(), "run id");
		assertEquals(-100, record.getScore(), "score can follow game state");
		assertEquals(4, record.getEnemiesDefeated(), "enemies defeated");
		assertEquals(2500L, record.getActiveDurationMillis(),
				"active duration");
		assertEquals(123456L, record.getCompletionTimeMillis(),
				"completion time");

		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				new RunRecord(null, 0, 0, 0, 1);
			}
		}, "null run id");
		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				new RunRecord("  ", 0, 0, 0, 1);
			}
		}, "blank run id");
		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				new RunRecord("bad-enemies", 0, -1, 0, 1);
			}
		}, "negative enemies");
		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				new RunRecord("bad-duration", 0, 0, -1, 1);
			}
		}, "negative duration");
		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				new RunRecord("bad-time", 0, 0, 0, 0);
			}
		}, "non-positive completion time");
	}

private static void testRepositoryDeduplication() {
		final RunRecordRepository repository =
				new InMemoryRunRecordRepository();
		RunRecord first = new RunRecord("run-a", 100, 3, 2000, 10);
		RunRecord duplicate = new RunRecord("run-a", 999, 9, 9000, 99);
		RunRecord sameScore = new RunRecord("run-b", 100, 3, 2000, 20);

		assertEquals(0, repository.size(), "empty repository");
		assertTrue(repository.add(first), "first record is added");
		assertFalse(repository.add(duplicate), "duplicate run id is ignored");
		assertTrue(repository.add(sameScore),
				"same score with a new run id is added");
		assertEquals(2, repository.size(), "only unique runs are stored");

		final List<RunRecord> snapshot = repository.getAll();
		assertEquals(2, snapshot.size(), "snapshot size");
		assertSame(first, snapshot.get(0), "insertion order is retained");
		expectException(UnsupportedOperationException.class,
				new CheckedAction() {
					public void run() {
						snapshot.clear();
					}
				}, "snapshot cannot be modified");
		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				repository.add(null);
			}
		}, "null record");
	}

private static void testRunSessionTimingAndFinish() {
		RunSession session = new RunSession("run-timed");
		session.startActiveSegment(1000000000L);
		assertEquals(1000L, session.getActiveDurationMillis(2000000000L),
				"live active segment is visible to achievements");
		session.startActiveSegment(1500000000L);
		session.stopActiveSegment(3000000000L);
		session.stopActiveSegment(4000000000L);
		session.startActiveSegment(5000000000L);
		session.stopActiveSegment(8000000000L);

		RunRecord completed = session.finish(750, 12, 50000);
		assertEquals("run-timed", completed.getRunId(), "session run id");
		assertEquals(5000L, completed.getActiveDurationMillis(),
				"only active segments are accumulated");
		assertEquals(750, completed.getScore(), "session score");
		assertEquals(12, completed.getEnemiesDefeated(),
				"session enemies defeated");

		RunRecord duplicateFinish = session.finish(999, 99, 99999);
		assertSame(completed, duplicateFinish,
				"repeated finish returns the original record");
		assertEquals(750, duplicateFinish.getScore(),
				"repeated finish does not replace values");
		session.startActiveSegment(9000000000L);
		session.stopActiveSegment(12000000000L);
		assertEquals(5000L, session.finish(0, 0, 1)
				.getActiveDurationMillis(), "finished sessions no longer change");

		final RunSession activeSession = new RunSession("run-active");
		activeSession.startActiveSegment(10);
		expectException(IllegalStateException.class, new CheckedAction() {
			public void run() {
				activeSession.finish(0, 0, 1);
			}
		}, "active session must stop before finish");
		activeSession.stopActiveSegment(20);
		assertEquals(0L, activeSession.finish(0, 0, 1)
				.getActiveDurationMillis(), "sub-millisecond time rounds down");

		final RunSession invalidClock = new RunSession("run-clock");
		invalidClock.startActiveSegment(20);
		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				invalidClock.stopActiveSegment(19);
			}
		}, "clock cannot move backwards");
		invalidClock.stopActiveSegment(30);

		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				new RunSession("");
			}
		}, "session run id is required");
	}

private static void testFormatterOutput() {
		RunRecord record = new RunRecord("run-output", 800, 15, 12000,
				987654321);
		String output = RunRecordFormatter.format(record);
		assertContains(output, "Run ID: run-output", "formatted run id");
		assertContains(output, "Score: 800", "formatted score");
		assertContains(output, "Enemies defeated: 15",
				"formatted enemies");
		assertContains(output, "Active duration: 12000 ms (12.000 s)",
				"formatted duration");
		assertContains(output, "Completion time: 987654321",
				"formatted completion time");
		expectException(IllegalArgumentException.class, new CheckedAction() {
			public void run() {
				RunRecordFormatter.format(null);
			}
		}, "formatter rejects null");
	}

private static void testPersonalBests() {
    RecordsService service = new RecordsService();
    assertEquals(0, service.getHighestScore(), "empty score best");
    service.completeRun(new RunRecord("a", -50, 4, 9000, 1));
    service.completeRun(new RunRecord("b", -60, 7, 8000, 2));
    service.completeRun(new RunRecord("c", -50, 7, 9000, 3));
    assertEquals(-50, service.getHighestScore(), "score best and ties");
    assertEquals(7, service.getMostEnemiesDefeated(), "enemy best and ties");
    assertEquals(9000L, service.getLongestSurvivalMillis(), "duration best and ties");
}
private static void testRankingOrderAndLimit() {
		try {
			Path profile = testDirectory("invaders-ranking-test")
					.resolve("profile.properties");
			RecordsService service = new RecordsService();
			service.completeRun(new RunRecord("z", 100, 0, 0, 20));
			service.completeRun(new RunRecord("a", 100, 0, 0, 10));
			service.completeRun(new RunRecord("b", 100, 0, 0, 10));
			for (int i = 0; i < 9; i++)
				service.completeRun(new RunRecord("low-" + i, i, 0, 0, 30 + i));
			List<RunRecord> top = service.getTopRuns();
			assertEquals(10, top.size(), "ranking is capped at ten entries");
			assertEquals("a", top.get(0).getRunId(), "earlier tie sorts first");
			assertEquals("b", top.get(1).getRunId(), "run ID breaks timestamp tie");
			assertEquals("z", top.get(2).getRunId(), "later completion follows");

		} catch (IOException exception) {
			throw new AssertionError("ranking test failed", exception);
		}
	}

	private static void assertTrue(final boolean actual,
			final String message) {
		checks++;
		if (!actual)
			throw new AssertionError(message);
	}
	private static void assertFalse(final boolean actual,
			final String message) {
		assertTrue(!actual, message);
	}
	private static void assertEquals(final int expected, final int actual,
			final String message) {
		checks++;
		if (expected != actual)
			throw new AssertionError(message + ": expected " + expected
					+ " but was " + actual);
	}
	private static void assertEquals(final long expected, final long actual,
			final String message) {
		checks++;
		if (expected != actual)
			throw new AssertionError(message + ": expected " + expected
					+ " but was " + actual);
	}
	private static void assertEquals(final String expected,
			final String actual, final String message) {
		checks++;
		if (!expected.equals(actual))
			throw new AssertionError(message + ": expected " + expected
					+ " but was " + actual);
	}
	private static void assertSame(final Object expected, final Object actual,
			final String message) {
		checks++;
		if (expected != actual)
			throw new AssertionError(message);
	}
	private static void assertContains(final String actual,
			final String expectedPart, final String message) {
		checks++;
		if (!actual.contains(expectedPart))
			throw new AssertionError(message + ": missing " + expectedPart);
	}
	private static void expectException(
			final Class<? extends Throwable> expectedType,
			final CheckedAction action, final String message) {
		checks++;
		try {
			action.run();
		} catch (Throwable actual) {
			if (expectedType.isInstance(actual))
				return;
			throw new AssertionError(message + ": expected "
					+ expectedType.getSimpleName() + " but caught "
					+ actual.getClass().getSimpleName());
		}
		throw new AssertionError(message + ": expected "
				+ expectedType.getSimpleName());
	}
	private static Path testDirectory(final String prefix) throws IOException {
		Path root = Paths.get("out", "records-tests");
		Files.createDirectories(root);
		return Files.createTempDirectory(root, prefix);
	}
}
