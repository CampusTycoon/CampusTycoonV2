package com.spacecomplexity.longboilife.menu;

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
import com.badlogic.gdx.Input.Keys;

/**
 * Main class to control the menu screen.
 */
public class MenuScreen implements Screen {
    private final Main game;

    private Viewport viewport;

    private Texture[] backgroundTextures;
    private int currentBackgroundIndex = 0;
    private float backgroundTimer = 0;
    private static final float BACKGROUND_SWITCH_TIME = 3f; // Switch every 3 seconds

    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    public MenuScreen(Main game) {
        this.game = game;
        
        // Load user settings
        Settings.initialise();
        // Apply current window settings to the application
        Window.refresh();

        // Initialise viewport and drawing elements
        viewport = new ScalingViewport(Scaling.fit, Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        // Replace single background texture with array of textures
        backgroundTextures = new Texture[]{
            new Texture(Gdx.files.internal("ui/Example1.png")),
            new Texture(Gdx.files.internal("ui/Example2.png")),
            new Texture(Gdx.files.internal("ui/Example3.png"))
        };

        // Load UI skin for buttons
        skin = new Skin(Gdx.files.internal("ui/skin/uiskin.json"));
    }

    @Override
    public void show() {
        // Recreate stage if it was disposed
        if (stage == null) {
            stage = new Stage(viewport);
            skin = new Skin(Gdx.files.internal("ui/skin/uiskin.json"));
        }
        
        // Set up input processor
        Gdx.input.setInputProcessor(stage);
        
        // Table layout for menu alignment
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Initialise play button
        TextButton playButton = new TextButton("Play", skin, "round");
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Switch to game screen
                game.switchScreen(Main.ScreenType.GAME);
            }
        });

        // Initialise exit button
        TextButton exitButton = new TextButton("Exit", skin, "round");
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Exit the application
                Gdx.app.exit();
            }
        });

        // Initialise settings button
        TextButton settingsButton = new TextButton("Settings", skin, "round");
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.openSettings(Main.ScreenType.MENU);
            }
        });        

        // Initialise leaderboard button
        TextButton leaderboardButton = new TextButton("Leaderboard", skin, "round");
        leaderboardButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.openLeaderboard(Main.ScreenType.MENU);
            }
        });

        // Add buttons to table
        table.add(playButton);
        table.row();
        table.add(settingsButton).padTop(10);
        table.row();
        table.add(leaderboardButton).padTop(10);
        table.row();
        table.add(exitButton).padTop(10);

        
        // Position the table correctly
        table.pad(150).padBottom((Window.DEFAULT_HEIGHT / 2) - 40).bottom().right();

        // Create a custom input processor that handles ESC key
        InputMultiplexer inputMultiplexer = new InputMultiplexer(
            new MainInputManager() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Keys.ESCAPE) {
                        // Do nothing when ESC is pressed on menu screen1
                        return true; // Return true to indicate we handled the key press
                    }
                    return super.keyDown(keycode);
                }
            }, 
            stage
        );
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1f);

        // Update background timer and index
        backgroundTimer += delta;
        if (backgroundTimer >= BACKGROUND_SWITCH_TIME) {
            backgroundTimer = 0;
            currentBackgroundIndex = (currentBackgroundIndex + 1) % backgroundTextures.length;
        }

        // Draw current background image
        batch.begin();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.draw(backgroundTextures[currentBackgroundIndex], 
            0, 0,                           // Position
            viewport.getWorldWidth(),       // Width
            viewport.getWorldHeight());     // Height
        batch.end();

        // Draw the UI
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
        // Just clear the stage without disposing
        if (stage != null) {
            stage.clear();
        }
        // Remove input processor
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (skin != null) {
            skin.dispose();
            skin = null;
        }
        if (backgroundTextures != null) {
            for (Texture texture : backgroundTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            backgroundTextures = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }
}
