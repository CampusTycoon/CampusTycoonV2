package com.spacecomplexity.longboilife.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.Main;
import com.spacecomplexity.longboilife.MainInputManager;
import com.spacecomplexity.longboilife.game.globals.Settings;
import com.spacecomplexity.longboilife.game.globals.Window;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * Main class to control the menu screen.
 */
public class SettingsScreen implements Screen {
    private final Main game;
    private final Main.ScreenType previousScreen;

    private Viewport viewport;

    private Texture backgroundTexture;
    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    private TextButton fullscreenLabel;
    private TextButton resolutionLabel;

    public SettingsScreen(Main game, Main.ScreenType previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        // Initialise viewport and drawing elements
        viewport = new ScalingViewport(Scaling.fit, Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal("menu/Plain black.png"));

        // Load UI skin for buttons
        skin = new Skin(Gdx.files.internal("ui/skin/uiskin.json"));
    }

    @Override
    public void show() {
        // Clear any existing actors before setting up new ones
        stage.clear();
        
        // Table layout for menu alignment
        Table table = new Table();
        table.setFillParent(true);
        table.setSkin(skin);
        stage.addActor(table);

        // Resolution Functions
        // Initialise resolution table
        Table resolutionRow = new Table();
        resolutionRow.setSkin(skin);    // Set skin for a table to prevent app from crashing
        resolutionRow.add(new TextButton("Resolution:", skin, "round")).left(); // Add button and anchor it to the left
        resolutionRow.add().expandX();  // Makes the text go all the way to the right

        // Left cycle button for resolution
        TextButton leftResolutionButton = new TextButton("<", skin, "round");
        leftResolutionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: Add resolution cycling logic
            }
        });

        // Right cycle button for resolution
        TextButton rightResolutionButton = new TextButton(">", skin, "round");
        rightResolutionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: Add resolution cycling logic
            }
        });
        
        resolutionLabel = new TextButton(Gdx.graphics.isFullscreen() ? "1920x1080" : "1280x720", skin, "round");
        resolutionRow.add(leftResolutionButton).right().pad(10);
        resolutionRow.add(resolutionLabel).right().pad(10);
        resolutionRow.add(rightResolutionButton).right().pad(10);


        // Window Mode Functions
        // Initialise window mode table
        Table fullscreenRow = new Table();
        fullscreenRow.setSkin(skin);
        fullscreenRow.add(new TextButton("Fullscreen:", skin, "round")).left();
        fullscreenRow.add().expandX();
        
        // Left cycle button for fullscreen
        TextButton leftFullscreenButton = new TextButton("<", skin, "round");
        leftFullscreenButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleFullscreen();
            }
        });

        // Right cycle button for fullscreen
        TextButton rightFullscreenButton = new TextButton(">", skin, "round");
        rightFullscreenButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleFullscreen();
            }
        });

        // Logic for displaying the current window mode
        fullscreenLabel = new TextButton(Gdx.graphics.isFullscreen() ? "Fullscreen" : "Windowed", skin, "round");
        fullscreenRow.add(leftFullscreenButton).right().pad(10);
        fullscreenRow.add(fullscreenLabel).right().pad(10);
        fullscreenRow.add(rightFullscreenButton).right().pad(10);


        // Volume Control
        Table volumeRow = new Table();
        volumeRow.setSkin(skin);
        volumeRow.add(new TextButton("Volume:", skin, "round")).left();
        volumeRow.add().expandX();

        // Create slider for volume control
        Slider volumeSlider = new Slider(0f, 1f, 0.1f, false, skin);
        volumeSlider.setValue(Settings.volume); // Set default volume
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = volumeSlider.getValue();
                Settings.volume = volume;
            }
        });

        volumeRow.add(volumeSlider).right().padRight(120);

        // Save button
        Table saveRow = new Table();
        saveRow.setSkin(skin);
        saveRow.add().expandX();
        
        TextButton saveButton = new TextButton("Save", skin, "round");
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Settings.save();
                game.switchScreen(previousScreen);
            }
        });
        
        saveRow.add(saveButton).right().pad(10);

        // Add buttons to table
        table.add(resolutionRow).expandX().fillX().padTop(10);  // Adding the rows declared above to the render table
        table.row();
        table.add(fullscreenRow).expandX().fillX().padTop(10);
        table.row();
        table.add(volumeRow).expandX().fillX().padTop(20);
        table.row();
        table.add(saveRow).padBottom(10);   // TODO: Figure out how to anchor this to the bottom of the screen
        table.row();
     
        // Position the table correctly
        table.left().pad(50);
        table.top().pad(50);

        // Allows UI to capture touch events
        InputMultiplexer inputMultiplexer = new InputMultiplexer(new MainInputManager(), stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1f);

        // Draw background image
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Window.DEFAULT_HEIGHT, Window.DEFAULT_HEIGHT);
        batch.end();

        // Draw and apply ui
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        Window.update(width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        // Clear all actors from the stage when hiding the screen
        stage.clear();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
        batch.dispose();
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);
            fullscreenLabel.setText("Windowed");
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            fullscreenLabel.setText("Fullscreen");
        }
        // Clear and rebuild stage after changing screen mode
        stage.clear();
        show();
    }
}
