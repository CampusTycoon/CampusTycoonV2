package com.spacecomplexity.longboilife.game.utils;

import java.util.function.Function;

import com.spacecomplexity.longboilife.game.utils.Events.GameEvent;
import com.spacecomplexity.longboilife.game.world.World;
import com.spacecomplexity.longboilife.Main;
import com.spacecomplexity.longboilife.game.utils.Events.Event;

/**
 * Class to manage events which can be called from anywhere within the game.
 */
public class EventHandler {
    private static final EventHandler eventHandler = new EventHandler();
    
    public void initialiseEvents(Main game, World world) {
        Events events = new Events(game, world);
        events.initialiseEvents();
    }

    /**
     * Create an event.
     *
     * @param event    the event.
     * @param callback the event method, the function that will be executed on any calls.
     */
    public void createEvent(Event event, Function<Object[], Object> callback) {
        event.setCallback(callback);
    }
    /**
     * Create an event.
     *
     * @param event    the event.
     * @param callback the event method, the function that will be executed on any calls.
     */
    public void createEvent(GameEvent event, Function<Object[], Object> callback) {
        event.setCallback(callback);
    }

    /**
     * Call a previously defined event.
     *
     * @param event  the event given to call.
     * @param params the parameter(s) to pass to the event.
     * @return what the original event would return, this will need to be cast as we cannot know the type here.
     * @throws IllegalArgumentException if the event has not been defined.
     */
    public Object callEvent(Event event, Object... params) throws IllegalArgumentException {
        Function<Object[], Object> callback = event.getCallback();

        // If the callback is not defined then throw an error
        if (callback == null) {
            throw new IllegalArgumentException("No method defined for calling event: \"" + event.name() + "\"");
        }

        // Execute the callback and return the result
        return callback.apply(params);
    }
    
    /**
     * Call a previously defined game event.
     *
     * @param event  the game event given to call.
     * @param params the parameter(s) to pass to the event.
     * @return what the original event would return, this will need to be cast as we cannot know the type here.
     * @throws IllegalArgumentException if the event has not been defined.
     */
    public Object callEvent(GameEvent event, Object... params) throws IllegalArgumentException {
        Function<Object[], Object> callback = event.getCallback();

        // If the callback is not defined then throw an error
        if (callback == null) {
            throw new IllegalArgumentException("No method defined for calling event: \"" + event.name() + "\"");
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
