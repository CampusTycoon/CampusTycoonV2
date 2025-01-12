package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainTimer;
import com.spacecomplexity.longboilife.game.ui.UIElement;
import com.spacecomplexity.longboilife.game.utils.Events;
import com.spacecomplexity.longboilife.game.utils.EventHandler;
import com.spacecomplexity.longboilife.game.utils.UIUtils;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/**
 * Class to represent the Bottom Menu UI.
 */
public class UIBottomMenu extends UIElement {
    private final Texture pauseTexture;
    private final TextureRegionDrawable pauseDrawable;
    private final Texture playTexture;
    private final TextureRegionDrawable playDrawable;

    private UIBuildMenu buildMenu;
    private UIPauseScreen pauseScreen;

    /**
     * Initialise bottom menu elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIBottomMenu(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        EventHandler eventHandler = EventHandler.getEventHandler();

        buildMenu = new UIBuildMenu(uiViewport, parentTable, skin);
        pauseScreen = new UIPauseScreen(uiViewport, parentTable, skin);

        // Place building buttons on separate table for condensed styling
        Table buildingButtonsTable = new Table(skin);
        // Initialise building buttons
        for (BuildingCategory category : BuildingCategory.values()) {
            TextButton button = new TextButton(category.getDisplayName(), skin);

            // On click execute function to open the buildMenuTable on the specific category
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    buildMenu.openMenu(category);
                }
            });

            buildingButtonsTable.add(button).expandX().padLeft(2);
        }
        // Add the buttons onto the main table
        table.add(buildingButtonsTable).expandX().left();

        // Load play/pause textures as drawables
        float textureSize = 25;
        pauseTexture = new Texture(Gdx.files.internal("ui/buttons/pause.png"));
        pauseDrawable = new TextureRegionDrawable(pauseTexture);
        pauseDrawable.setMinSize(textureSize, textureSize);
        playTexture = new Texture(Gdx.files.internal("ui/buttons/play.png"));
        playDrawable = new TextureRegionDrawable(playTexture);
        playDrawable.setMinSize(textureSize, textureSize);

        // Initialise pause button
        ImageButton pauseButton = new ImageButton(skin);
        pauseButton.setSize(textureSize, textureSize);
        // Initially set background to ❚❚
        pauseButton.getStyle().up = pauseDrawable;
        pauseButton.getStyle().down = pauseDrawable;
        // Pause/resume the game when clicked
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Call the events to pause/resume the game based on the current pause state
                eventHandler.callEvent(GameState.getState().paused ? Events.Event.RESUME_GAME : Events.Event.PAUSE_GAME);
            }
        });
        // Place pause button on the table
        table.add(pauseButton).right().padRight(10);

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        table.setTouchable(Touchable.enabled);
        placeTable();

        // Assign pause and play events
        eventHandler.createEvent(Events.Event.PAUSE_GAME, (params) -> {
            // Set pause state
            GameState.getState().paused = true;
            // Pause the timer
            MainTimer.getTimerManager().getTimer().pauseTimer();
            // Cancel all actions
            eventHandler.callEvent(Events.Event.CANCEL_OPERATIONS);
            // Disable all UI but the pause button
            UIUtils.disableAllActors(parentTable.getStage());
            UIUtils.enableActor(pauseButton);
            // Change background to ▶
            pauseButton.getStyle().up = playDrawable;
            pauseButton.getStyle().down = playDrawable;

            return null;
        });
        eventHandler.createEvent(Events.Event.RESUME_GAME, (params) -> {
            // Set pause state
            GameState.getState().paused = false;
            // Resume the timer
            MainTimer.getTimerManager().getTimer().resumeTimer();
            // Re enable all UI
            UIUtils.enableAllActors(parentTable.getStage());
            // Change background to ❚❚
            pauseButton.getStyle().up = pauseDrawable;
            pauseButton.getStyle().down = pauseDrawable;

            return null;
        });
    }

    public void render() {
        buildMenu.render();
        pauseScreen.render();
    }

    @Override
    public void resize() {
        super.resize();

        buildMenu.resize();
        pauseScreen.resize();
    }

    @Override
    protected void placeTable() {
        table.setSize(uiViewport.getWorldWidth(), 60);
        table.setPosition(0, 0);
    }

    @Override
    public void dispose() {
        super.dispose();

        playTexture.dispose();
        pauseTexture.dispose();

        buildMenu.dispose();
        pauseScreen.dispose();
    }
}
