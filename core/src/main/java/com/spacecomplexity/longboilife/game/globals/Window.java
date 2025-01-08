package com.spacecomplexity.longboilife.game.globals;

import com.badlogic.gdx.Gdx;

/**
 * Class to contain the current window state.
 */
public final class Window {
    /**
    * The default width of the window.
    * <p>
    * Some things in LibGDX are tied to this initial value.
    */
    public static final int DEFAULT_WIDTH = 1080;

    /**
    * The default height of the window.
    * <p>
    * Some things in LibGDX are tied to this initial value.
    */
    public static final int DEFAULT_HEIGHT = 720;

    /**
     * Whether or not the window is maximised.
     */
    public static Boolean isFullscreen;

    /**
     * The current width (in pixels) of the window.
     */
    public static int width;

    /**
     * The current height (in pixels) of the window.
     */
    public static int height;


    /**
     * Updates globals when the window is resized.
     */
    public static void update(int Width, int Height) {
        width = Width;
        height = Height;
    }
    
    /**
     * Applies the current window settings.
     */
    public static void refresh() {
        if (isFullscreen == null) {
            return;
        }
        
        if (isFullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
        else {
            Gdx.graphics.setWindowedMode(width, height);
        }
    }
}
