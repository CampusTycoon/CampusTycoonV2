package com.spacecomplexity.longboilife.game.utils;

/**
 * Class to represent a simple timer
 */
public class Timer {
    private long finishTime;
    private long onPauseTime;
    private boolean paused;

    private boolean eventCalled;
    private Runnable event;

    /**
     * Create a new timer object.
     * <p>
     * {@link Timer#setTimer(long)} and {@link Timer#setEvent(Runnable)} should be used to initialise the timer.
     */
    public Timer() {
    }

    public void setEvent(Runnable event) {
        this.event = event;
    }

    /**
     * Start a new timer with specified duration.
     *
     * @param duration duration of timer in ms.
     */
    public void setTimer(long duration) {
        finishTime = System.currentTimeMillis() + duration;
        paused = false;
        eventCalled = false;
    }

    /**
     * Pause the currently running timer.
     */
    public void pauseTimer() {
        onPauseTime = System.currentTimeMillis();
        paused = true;
    }

    /**
     * Resume the paused timer.
     *
     * @throws IllegalStateException if the timer has not been previously paused.
     */
    public void resumeTimer() throws IllegalStateException {
        if (!paused) {
            throw new IllegalStateException("Timer has not been paused");
        }

        // Calculate the time the timer has been paused for and add this onto the finishing time
        long pausedTime = System.currentTimeMillis() - onPauseTime;
        finishTime += pausedTime;

        paused = false;
    }

    /**
     * Retrieve how much time is left for the timer.
     *
     * @return the time left in ms.
     */
    public long getTimeLeft() {
        if (paused) {
            return finishTime - onPauseTime;
        }

        return finishTime - System.currentTimeMillis();
    }

    /**
     * Return whether the timer is currently paused.
     *
     * @return whether the timer is currently paused.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Polls the current time and will run the event if this time has passed.
     *
     * @return whether the time has passed.
     */
    public boolean poll() {
        if (getTimeLeft() <= 0) {
            if (!eventCalled) {
                event.run();
                eventCalled = true;
            }
            
            return true;
        }

        return false;
    }
}
