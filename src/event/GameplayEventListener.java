package event;

/**
 * Receives one specific type of gameplay event.
 *
 * @param <T> event type handled by this listener
 */
public interface GameplayEventListener<T extends GameplayEvent> {

    void onEvent(T event);
}