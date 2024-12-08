package com.spacecomplexity.longboilife.game.globals;

import com.badlogic.gdx.Input;

/**
 * Stores the keybindings of different actions.
 */
public enum Keybindings {
    CAMERA_UP(Input.Keys.W),
    CAMERA_LEFT(Input.Keys.A),
    CAMERA_DOWN(Input.Keys.S),
    CAMERA_RIGHT(Input.Keys.D),
    CAMERA_ZOOM_IN(Input.Keys.Q),
    CAMERA_ZOOM_OUT(Input.Keys.E),
    FULLSCREEN(Input.Keys.F11),
    CANCEL(Input.Keys.ESCAPE),
    PAUSE(Input.Keys.SPACE),
    SETTINGS_MENU(Input.Keys.ESCAPE),
    ;

    private final int key;

    /**
     * Initialises a key to an enum attribute.
     *
     * @param key the key assigned to the enum attribute.
     */
    Keybindings(int key) {
        this.key = key;
    }

    /**
     * Return the key code assigned to the enum attribute.
     *
     * @return the key code assigned.
     */
    public int getKey() {
        return key;
    }
}
