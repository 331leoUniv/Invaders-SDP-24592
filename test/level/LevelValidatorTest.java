package level;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevelValidatorTest {
    private final LevelValidator validator = new LevelValidator(LevelImportPolicy.defaults());

    @Test
    void preservesSparseDefinitionsAndBaseValues() throws Exception {
        EnemyCell[][] grid = { { null, new EnemyCell("A", 10), null }, { null, null, null } };
        WaveDefinition wave = new WaveDefinition("wave-01", grid, 40, 120, 0, 100);
        LevelDefinition level = level("Stage", wave);
        validator.validate(level);
        assertSame(wave, level.getWaves().get(0));
        assertEquals(2, wave.getRows());
        assertEquals(3, wave.getColumns());
        assertNull(wave.getCell(0, 0));
        assertNull(wave.getCell(1, 1));
        assertEquals(40, wave.getInitialX());
        assertEquals(120, wave.getInitialY());
        assertEquals(0, wave.getMovementBaseFrames());
    }

    @Test
    void reportsUnknownEnemyAndHealthWithTheirCellLocation() {
        rejected(level("Stage", wave("boss", 1, 1, 1, 60, 1000)),
                "waves[0].formation.grid[0][0].enemyType");
        rejected(level("Stage", wave("A", 11, 1, 1, 60, 1000)),
                "waves[0].formation.grid[0][0].health");
    }

    @Test
    void allowsTheCallerToReplaceProvisionalLimitsAndRegistry() throws Exception {
        LevelImportPolicy policy = LevelImportPolicy.builder()
                .enemyTypes(Arrays.asList("boss")).maxEnemyHealth(20)
                .maxRows(9).maxColumns(10).maxMovementBaseFrames(1000)
                .minShootingIntervalMs(1).maxShootingIntervalMs(100000).build();
        new LevelValidator(policy).validate(level("Custom",
                wave("boss", 20, 9, 10, 1000, 1)));
        new LevelValidator(policy).validate(level("Custom",
                wave("boss", 20, 9, 10, 1000, 100000)));
    }

    private void rejected(final LevelDefinition definition, final String path) {
        LevelFormatException error = assertThrows(LevelFormatException.class,
                () -> validator.validate(definition));
        assertTrue(error.getMessage().startsWith(path + ":"), error.getMessage());
    }

    private static LevelDefinition level(final String name, final WaveDefinition wave) {
        return new LevelDefinition(1, "stage", name, Collections.singletonList(wave));
    }

    private static WaveDefinition wave(final String type, final int health,
            final int rows, final int columns, final int movement, final int shooting) {
        EnemyCell[][] grid = new EnemyCell[rows][columns];
        grid[0][0] = new EnemyCell(type, health);
        return new WaveDefinition("wave-01", grid, 20, 100, movement, shooting);
    }
}
