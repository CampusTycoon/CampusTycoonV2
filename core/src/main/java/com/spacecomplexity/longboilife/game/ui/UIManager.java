package com.spacecomplexity.longboilife.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.Window;
import com.spacecomplexity.longboilife.game.ui.game.*;
import com.spacecomplexity.longboilife.game.ui.gameover.UIOverview;
import com.spacecomplexity.longboilife.game.utils.EventHandler;
import com.spacecomplexity.longboilife.game.utils.Events;
import com.spacecomplexity.longboilife.achievements.AchievementManager;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.spacecomplexity.longboilife.game.utils.UIUtils;

/**
 * Class to manage the UI in the game.
 */
public class UIManager {
    private Viewport viewport;

    private Stage stage;
    private Skin skin;

    private UIElement[] uiElements;
    private UIEventPopup eventPopup;

    /**
     * Initialise UI elements needed for the game.
     *
     * @param inputMultiplexer to add the UI events to the input processing
     */
    public UIManager(InputMultiplexer inputMultiplexer) {
        // Initialise viewport for rescaling
        viewport = new ScalingViewport(Scaling.fit, Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);

        // Initialise stage
        stage = new Stage(viewport);
        inputMultiplexer.addProcessor(stage);

        // Initialise root table
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Load external UI skin
        skin = new Skin(Gdx.files.internal("ui/skin/uiskin.json"));

        // Load external font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/fonts/Roboto-Medium.ttf"));
        // Generate a bitmap font for size 12
        BitmapFont ourFont12 = generator.generateFont(new FreeTypeFontGenerator.FreeTypeFontParameter() {{
            size = 12;
        }});
        // Generate a bitmap font for size 14
        BitmapFont ourFont16 = generator.generateFont(new FreeTypeFontGenerator.FreeTypeFontParameter() {{
            size = 14;
        }});
        generator.dispose();

        // Set skins font to our font
        skin.get("default", Label.LabelStyle.class).font = ourFont12;
        skin.get("default", TextButton.TextButtonStyle.class).font = ourFont16;

        // Create our UI elements
        // Note: The order of these is the order that they will be rendered
        eventPopup = new UIEventPopup(viewport, table, skin);
        uiElements = new UIElement[]{
            eventPopup,
            new UITooltip(viewport, table, skin),
            new UIBuildingSelectedMenu(viewport, table, skin),
            new UIBottomMenu(viewport, table, skin),
            new UIClockMenu(viewport, table, skin),
            new UISatisfactionMenu(viewport, table, skin),
            new UIMoneyMenu(viewport, table, skin),
            new UIBuildingCounter(viewport, table, skin),
        };

        // Initialize achievement notifications
        AchievementManager.getInstance().initializeNotification(viewport, table, skin);

        // Hide game UI and show end UI
        EventHandler.getEventHandler().createEvent(Events.Event.GAME_END, (params) -> {
            GameState.getState().gameOver = true;
            GameState.getState().active = false;
            
            // Log total events that occurred
            System.out.println("Total events during game: " + GameState.getState().getTotalEvents());
            
            // Cancel all operations and close building mode/menus
            EventHandler.getEventHandler().callEvent(Events.Event.CANCEL_OPERATIONS);
            GameState.getState().buildMenuOpen = false;
            GameState.getState().placingBuilding = null;
            
            // Disable all UI interaction
            UIUtils.disableAllActors(stage);

            // Check achievements before any UI changes
            AchievementManager.getInstance().checkAchievements();

            // Add a delay to allow the achievement notification to show
            stage.addAction(Actions.sequence(
                Actions.delay(4f), // Wait for notification to finish (3s display + 0.5s fade out + buffer)
                Actions.run(() -> {
                    // Run dispose functions on UI elements
                    for (UIElement uiElement : uiElements) {
                        uiElement.dispose();
                    }
                    table.clear();

                    // Create the new end elements
                    uiElements = new UIElement[]{
                        new UIOverview(viewport, table, skin),
                    };
                    
                    // Re-enable UI interaction for the game over screen
                    UIUtils.enableAllActors(stage);
                })
            ));

            return null;
        });
    }

    /**
     * Apply and draw UI onto the screen.
     */
    public void render() {
        // Render on each of the UI elements
        for (UIElement uiElement : uiElements) {
            uiElement.render();
        }

        // Apply and then draw
        viewport.apply();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    /**
     * Handles resizing events, to ensure the UI is scaled correctly.
     *
     * @param width  the new width in pixels.
     * @param height the new height in pixels.
     */
    public void resize(int width, int height) {
        // Update world size to match scaling of uiScaleFactor
        viewport.setWorldSize(
            (float) width / GameState.getState().uiScaleFactor,
            (float) height / GameState.getState().uiScaleFactor
        );

        // Updates viewport to match new window size
        viewport.update(width, height, true);

        // Run resize functions on UI elements
        for (UIElement uiElement : uiElements) {
            uiElement.resize();
        }
    }

    /**
     * Dispose of all loaded assets.
     */
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        
        if (skin != null) {
            skin.dispose();
            skin = null;
        }

        if (uiElements != null) {
            for (UIElement uiElement : uiElements) {
                if (uiElement != null) {
                    uiElement.dispose();
                }
            }
            uiElements = null;
        }
    }

    public UIEventPopup getEventPopup() {
        return eventPopup;
    }
}
