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
import com.spacecomplexity.longboilife.game.globals.Window;

/**
 * Main class to control the menu screen.
 */
public class SettingsScreen implements Screen {
    private final Main game;

    private Viewport viewport;

    private Texture backgroundTexture;
    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    private TextButton fullscreenLabel;
    private TextButton resolutionLabel;

    public SettingsScreen(Main game) {
        this.game = game;

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
        // Table layout for menu alignment
        Table table = new Table();
        table.setFillParent(true);
        table.setSkin(skin);
        stage.addActor(table);



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


        // Initialise resolution table
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


        // Add buttons to table
        table.add(resolutionRow).expandX().fillX().padTop(10);  // Adding the rows declared above to the render table
        table.row();
        table.add(fullscreenRow).expandX().fillX().padTop(10);
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
    }
}
