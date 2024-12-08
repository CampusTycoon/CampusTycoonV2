package com.spacecomplexity.longboilife.game.utils;

import java.util.function.Function;

/**
 * CLass to manage events which can be called from anywhere within the game.
 */
public class EventHandler {
    public enum Event {
        BUILD,
        SELECT_BUILDING,
        CANCEL_OPERATIONS,
        SELL_BUILDING,
        MOVE_BUILDING,
        PAUSE_GAME,
        RESUME_GAME,
        OPEN_SELECTED_MENU,
        CLOSE_SELECTED_MENU,
        CLOSE_BUILD_MENU,
        GAME_END,
        RETURN_MENU,
        SETTINGS_MENU,
        ;

        private Function<Object[], Object> callback;

        private void setCallback(Function<Object[], Object> callback) {
            this.callback = callback;
        }

        private Function<Object[], Object> getCallback() {
            return callback;
        }
    }

    private static final EventHandler eventHandler = new EventHandler();

    /**
     * Create an event.
     *
     * @param event    the enum the event, needed when called.
     * @param callback the event method, this is what will be executed.
     */
    public void createEvent(Event event, Function<Object[], Object> callback) {
        event.setCallback(callback);
    }

    /**
     * Call a previously defined event.
     *
     * @param event  the enum of the event defined.
     * @param params the parameter to pass to the event.
     * @return what the original event would return, this will need to be cast as we cannot know the type here.
     * @throws IllegalArgumentException if the event has not been defined.
     */
    public Object callEvent(Event event, Object... params) throws IllegalArgumentException {
        Function<Object[], Object> callback = event.getCallback();

        // If the callback is not defined then throw an error
        if (callback == null) {
            throw new IllegalArgumentException("No event defined for: \"" + event.name() + "\"");
        }

        // Execute the callback and return the result
        return callback.apply(params);
    }

    /**
     * Get the singleton instance of the {@link EventHandler} class.
     *
     * @return The single {@link EventHandler} class.
     */
    public static EventHandler getEventHandler() {
        return eventHandler;
    }
}
