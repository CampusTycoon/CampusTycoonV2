package com.spacecomplexity.longboilife.game.globals;

import com.spacecomplexity.longboilife.game.utils.Timer;

/**
 * Singleton class to contain the global timer used within the game.
 */
public class MainTimer {
    private static final MainTimer mainTimer = new MainTimer();

    private final Timer timer;

    private MainTimer() {
        timer = new Timer();
    }

    /**
     * Get the singleton instance of the {@link MainTimer} class.
     *
     * @return The single {@link MainTimer} class.
     */
    public static MainTimer getTimerManager() {
        return mainTimer;
    }

    public Timer getTimer() {
        return timer;
    }
}
