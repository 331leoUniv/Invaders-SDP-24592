package event;

/**
 * Reports that the player destroyed one enemy in infinite mode.
 *
 * The event carries no data because KillAchievementTracker counts
 * each received event as one defeat.
 */
public final class InfiniteEnemyDefeatedEvent implements GameplayEvent {
    
}