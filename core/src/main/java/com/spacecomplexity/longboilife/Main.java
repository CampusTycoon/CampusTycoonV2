package com.spacecomplexity.longboilife;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.spacecomplexity.longboilife.game.GameScreen;
import com.spacecomplexity.longboilife.menu.MenuScreen;

import java.util.HashMap;

/**
 * The main class (entry point).
 * Responsible for managing and switching screens.
 */
public class Main extends Game {
    /**
     * If the game is in fullscreen mode.
     */
    public static boolean fullscreen = false;
    /**
     * The previous dimensions of the game, for returning from fullscreen.
     */
    public static int prevAppWidth, prevAppHeight;

    /**
     * Enum containing all screens and there class references.
     */
    public enum ScreenType {
        MENU(MenuScreen.class),
        GAME(GameScreen.class),
        ;

        private final Class<? extends Screen> screenClass;

        ScreenType(Class<? extends Screen> screenClass) {
            this.screenClass = screenClass;
        }

        public Class<? extends Screen> getScreenClass() {
            return screenClass;
        }
    }

    private HashMap<ScreenType, Screen> screens = new HashMap<>();

    @Override
    public void create() {
        // Initially load the menu screen
        switchScreen(ScreenType.MENU);
    }

    /**
     * Show a screen.
     *
     * @param screen the screen to show.
     */
    public void switchScreen(ScreenType screen) {
        // Lazy loading
        if (!screens.containsKey(screen)) {
            try {
                Screen newScreen = screen.getScreenClass().getConstructor(Main.class).newInstance(this);
                screens.put(screen, newScreen);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create screen: " + screen.name(), e);
            }
        }

        // Switch to the screen
        setScreen(screens.get(screen));
    }

    @Override
    public void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }

        super.dispose();
    }
}
