package com.spacecomplexity.longboilife;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.spacecomplexity.longboilife.game.GameScreen;
import com.spacecomplexity.longboilife.menu.MenuScreen;
import com.spacecomplexity.longboilife.settings.SettingsScreen;
import com.spacecomplexity.longboilife.leaderboard.LeaderboardScreen;
import java.util.HashMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;

/**
 * The main class (entry point).
 * Responsible for managing and switching screens.5
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
     * Enum containing all screens and their class references.
     */
    public enum ScreenType {
        MENU(MenuScreen.class),
        GAME(GameScreen.class),
        SETTINGS(SettingsScreen.class),
        LEADERBOARD(LeaderboardScreen.class);

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
        Gdx.app.addLifecycleListener(new LifecycleListener() {
            @Override
            public void pause() {}

            @Override
            public void resume() {}

            @Override
            public void dispose() {
                // Properly dispose of the current screen
                if (screen != null) {
                    screen.dispose();
                }
            }
        });

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

    public void openSettings(ScreenType previousScreen) {
        try {
            Screen settingsScreen = SettingsScreen.class.getConstructor(Main.class, ScreenType.class)
                .newInstance(this, previousScreen);
            screens.put(ScreenType.SETTINGS, settingsScreen);
            setScreen(settingsScreen);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create settings screen", e);
        }
    }

    public void openLeaderboard(ScreenType previousScreen) {
        try {
            Screen leaderboardScreen = LeaderboardScreen.class.getConstructor(Main.class, ScreenType.class)
                .newInstance(this, previousScreen);
            screens.put(ScreenType.LEADERBOARD, leaderboardScreen);
            setScreen(leaderboardScreen);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create leaderboard screen", e);
        }
    }

    @Override
    public void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }

        super.dispose();
    }
}