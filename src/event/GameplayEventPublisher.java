package event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Publishes gameplay events to listeners registered for each event type.
 *
 * Event delivery is synchronous. A failing listener is logged and does
 * not prevent the remaining listeners from receiving the event.
 * 
 * 각 이벤트 타입별로 등록된 리스너(listener)들에게 게임 플레이 이벤트를 전달합니다.
 * 이벤트 전달은 **동기식(synchronous)**으로 이루어집니다.
 * 어떤 리스너에서 오류가 발생하더라도 해당 오류는 로그에 기록될 뿐이며, 
 * 나머지 리스너들이 이벤트를 전달받는 것을 방해하지 않습니다.
 */
public final class GameplayEventPublisher {

    private static final Logger LOGGER =
            Logger.getLogger(GameplayEventPublisher.class.getName());

    private final Map<Class<? extends GameplayEvent>,
            Set<GameplayEventListener<? extends GameplayEvent>>> listeners;

    public GameplayEventPublisher() {
        this.listeners =
                new HashMap<Class<? extends GameplayEvent>,
                        Set<GameplayEventListener<? extends GameplayEvent>>>();
    }

    /**
     * Registers a listener for one concrete event type.
     * 
     * 하나의 구체적인 이벤트 타입에 대해 리스너를 등록합니다.
     */
    public <T extends GameplayEvent> void subscribe(
            final Class<T> eventType,
            final GameplayEventListener<T> listener) {

        if (eventType == null)
            throw new IllegalArgumentException("eventType must not be null");

        if (listener == null)
            throw new IllegalArgumentException("listener must not be null");

        Set<GameplayEventListener<? extends GameplayEvent>>
                eventListeners = this.listeners.get(eventType);

        if (eventListeners == null) {
            eventListeners =
                    new LinkedHashSet<GameplayEventListener <? extends GameplayEvent>>();

            this.listeners.put(eventType, eventListeners);
        }

        eventListeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     * 
     * “이전에 등록된 리스너를 제거합니다.
     */
    public <T extends GameplayEvent> void unsubscribe(
            final Class<T> eventType,
            final GameplayEventListener<T> listener) {

        if (eventType == null || listener == null)
            return;

        Set<GameplayEventListener<? extends GameplayEvent>>
                eventListeners = this.listeners.get(eventType);

        if (eventListeners == null)
            return;

        eventListeners.remove(listener);

        if (eventListeners.isEmpty()) 
            this.listeners.remove(eventType);
    }

    /**
     * Publishes an event to listeners registered for its concrete class.
     * 
     * 해당 이벤트의 구체적인 클래스에 등록된 리스너들에게 이벤트를 전달합니다.
     */
    public <T extends GameplayEvent> void publish(final T event) {

        if (event == null)
            throw new IllegalArgumentException("event must not be null");

        Set<GameplayEventListener<? extends GameplayEvent>>
                registeredListeners = this.listeners.get(event.getClass());

        if (registeredListeners == null)
            return;

        List<GameplayEventListener<? extends GameplayEvent>> snapshot =
                new ArrayList<GameplayEventListener
                        <? extends GameplayEvent>>(registeredListeners);

        for (GameplayEventListener<? extends GameplayEvent> listener : snapshot) {
            try {
                notifyListener(listener, event);
            } catch (RuntimeException exception) {
                LOGGER.log(
                    Level.SEVERE, "Gameplay event listener failed for " 
                    + event.getClass().getName(), exception);
            }
        }
    }

    /**
     * Keeps the unchecked cast inside the publisher.
     *
     * The subscribe method guarantees that listeners are stored under
     * the event class they accept.
     * 
     * 검사되지 않은 형 변환(unchecked cast)을 publisher 내부에만 유지합니다.
     * subscribe 메서드는 리스너가 해당 리스너가 받아들이는 이벤트 클래스 아래에 저장되도록 보장합니다.
     */
    @SuppressWarnings("unchecked")
    private <T extends GameplayEvent> void notifyListener(
            final GameplayEventListener<? extends GameplayEvent> listener, final T event) {

        GameplayEventListener<T> typedListener = (GameplayEventListener<T>) listener;

        typedListener.onEvent(event);
    }
}