package screen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import engine.GameSettings;
import engine.GameState;
import engine.GameplayConfig;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.PlayerStatus;
import event.GameplayEventListener;
import event.LevelCompletedEvent;

/** Standalone checks for level completion event publication. */
public final class LevelCompletedEventTest {
	public static void main(final String[] args) throws Exception {
		final int savedDifficulty = GameplayConfig.getDifficulty();
		try {
			clearPublishesOnce();
			onlyFinalWavePublishes();
			failureDoesNotPublish();
			difficultyIsCapturedAtStart(savedDifficulty);
		} finally {
			GameplayConfig.setDifficulty(savedDifficulty);
		}
		System.out.println("Level completed event checks passed.");
	}

	private static void clearPublishesOnce() throws Exception {
		GameScreen screen = newScreen(2, wave(1));
		List<LevelCompletedEvent> events = listen(screen);
		clearFormation(screen);
		screen.advanceStageIfReady();
		screen.advanceStageIfReady();
		check(events.size() == 1, "Stage clear must publish exactly once");
		check(events.get(0).getLevelId() == 2, "Event must report the completed stage");
		check(events.get(0).getDifficulty() == GameplayConfig.getDifficulty(),
				"Event must report the applied difficulty");
	}

	private static void onlyFinalWavePublishes() throws Exception {
		GameScreen screen = newScreen(1, wave(1), wave(2));
		List<LevelCompletedEvent> events = listen(screen);
		clearFormation(screen);
		screen.advanceStageIfReady();
		check(events.isEmpty(), "A wave clear must not publish a level completion");
		clearFormation(screen);
		screen.advanceStageIfReady();
		check(events.size() == 1, "The final wave clear must publish once");
	}

	private static void failureDoesNotPublish() throws Exception {
		GameScreen screen = newScreen(1, wave(1));
		List<LevelCompletedEvent> events = listen(screen);
		PlayerStatus status = (PlayerStatus) get(screen, "playerStatus");
		while (status.isAlive())
			status.takeDamage(1);
		clearFormation(screen);
		screen.advanceStageIfReady();
		check(events.isEmpty(), "A failed stage must not publish a completion");
		check(!screen.isStageCleared(), "Simultaneous death must fail the stage");
	}

	private static void difficultyIsCapturedAtStart(final int saved)
			throws Exception {
		int started = saved == GameplayConfig.HARD
				? GameplayConfig.EASY : GameplayConfig.HARD;
		GameplayConfig.setDifficulty(started);
		GameScreen screen = newScreen(1, wave(1));
		List<LevelCompletedEvent> events = listen(screen);
		GameplayConfig.setDifficulty(GameplayConfig.NORMAL == started
				? GameplayConfig.EASY : GameplayConfig.NORMAL);
		clearFormation(screen);
		screen.advanceStageIfReady();
		check(events.get(0).getDifficulty() == started,
				"A later preference change must not alter the reported difficulty");
	}

	private static GameSettings wave(final int width) {
		return new GameSettings(width, 1, 60, 2000);
	}

	private static GameScreen newScreen(final int level,
			final GameSettings... waves) {
		GameScreen screen = new GameScreen(new GameState(level, 0, 3, 0, 0),
				Arrays.asList(waves), false, 448, 520, 60);
		screen.initialize();
		return screen;
	}

	private static List<LevelCompletedEvent> listen(final GameScreen screen) {
		final List<LevelCompletedEvent> events = new ArrayList<LevelCompletedEvent>();
		screen.getGameplayEvents().subscribe(LevelCompletedEvent.class,
				new GameplayEventListener<LevelCompletedEvent>() {
					public void onEvent(final LevelCompletedEvent event) {
						events.add(event);
					}
				});
		return events;
	}

	private static void clearFormation(final GameScreen screen) throws Exception {
		EnemyShipFormation formation =
				(EnemyShipFormation) get(screen, "enemyShipFormation");
		List<EnemyShip> enemies = new ArrayList<EnemyShip>();
		for (EnemyShip enemy : formation)
			enemies.add(enemy);
		for (EnemyShip enemy : enemies)
			formation.destroy(enemy);
	}

	private static Object get(final Object target, final String name)
			throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}

	private static void check(final boolean condition, final String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	private LevelCompletedEventTest() {
	}
}