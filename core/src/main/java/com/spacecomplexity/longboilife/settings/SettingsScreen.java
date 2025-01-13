package com.spacecomplexity.longboilife.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainTimer;
import com.spacecomplexity.longboilife.game.globals.Settings;
import com.spacecomplexity.longboilife.game.globals.Window;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;
import com.spacecomplexity.longboilife.game.GameScreen;
import com.badlogic.gdx.Input.Keys;

/**
 * Main class to control the menu screen.
 */
public class SettingsScreen implements Screen {
    private final Main game;
    private final Main.ScreenType previousScreen;

    private Viewport viewport;

    private Texture[] backgroundTextures;
    private int currentBackgroundIndex = 0;
    private float backgroundTimer = 0;
    private static final float BACKGROUND_SWITCH_TIME = 3f;

    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    private TextButton fullscreenLabel;
    private TextButton resolutionLabel;

    private ShaderProgram blurShader;
    private static final float BLUR_RADIUS = 2.5f;
    private Texture gameScreenTexture;  // Added to store game screen capture

    public SettingsScreen(Main game, Main.ScreenType previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        
        // Pauses the game so that when the settings menu is closed the game can know to resume instead of restart
        GameState.getState().paused = true;
        MainTimer.getTimerManager().getTimer().pauseTimer();

        // Initialise viewport and drawing elements
        viewport = new ScalingViewport(Scaling.fit, Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        // Only load background textures if not coming from game screen
        if (previousScreen != Main.ScreenType.GAME) {
            backgroundTextures = new Texture[]{
                new Texture(Gdx.files.internal("ui/Example1.png")),
                new Texture(Gdx.files.internal("ui/Example2.png")),
                new Texture(Gdx.files.internal("ui/Example3.png"))
            };
        } else {
            // Capture the game screen
            int w = Gdx.graphics.getWidth();
            int h = Gdx.graphics.getHeight();

            // Get the game screen instance
            GameScreen gameScreen = game.getCurrentGameScreen();
            if (gameScreen != null) {
                // Render the world without UI
                gameScreen.renderWorldOnly();
                
                // Capture the frame buffer
                byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
                
                // Create a new texture from the pixels
                Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
                gameScreenTexture = new Texture(pixmap);
                pixmap.dispose();
            }
        }

        // Load and compile shaders
        ShaderProgram.pedantic = false;
        try {
            String vertexShader = Gdx.files.internal("shaders/blur.vert").readString();
            String fragmentShader = Gdx.files.internal("shaders/blur.frag").readString();
            
            if (vertexShader == null || fragmentShader == null) {
                throw new RuntimeException("Failed to load shader files");
            }
            
            blurShader = new ShaderProgram(vertexShader, fragmentShader);
            
            if (!blurShader.isCompiled()) {
                Gdx.app.error("Shader", "Shader compilation failed:\n" + blurShader.getLog());
                throw new RuntimeException("Shader compilation failed: " + blurShader.getLog());
            }
        } catch (Exception e) {
            Gdx.app.error("Shader", "Failed to load shaders: " + e.getMessage());
            throw new RuntimeException("Failed to load shaders", e);
        }

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
                toggleFullscreen();
            }
        });

        // Right cycle button for resolution
        TextButton rightResolutionButton = new TextButton(">", skin, "round");
        rightResolutionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleFullscreen();
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
        table.add(saveRow).padBottom(10);
        table.row();
     
        // Position the table correctly
        table.left().pad(50);
        table.top().pad(50);

        // Create menu button table only if coming from game screen
        if (previousScreen == Main.ScreenType.GAME) {
            Table menuButtonTable = new Table();
            menuButtonTable.setFillParent(true);
            stage.addActor(menuButtonTable);

            // Create menu button
            TextButton menuButton = new TextButton("Main Menu", skin, "round");
            menuButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.switchScreen(Main.ScreenType.MENU);
                }
            });

            // Add button to table and position at bottom center
            menuButtonTable.bottom().pad(20);
            menuButtonTable.add(menuButton);
        }

        // Create a custom input processor that handles ESC key
        InputMultiplexer inputMultiplexer = new InputMultiplexer(
            new MainInputManager() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Keys.ESCAPE) {
                        // Save settings and return to previous screen
                        Settings.save();
                        game.switchScreen(previousScreen);
                        return true;
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
        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1f);

        if (previousScreen == Main.ScreenType.GAME && gameScreenTexture != null) {
            // Draw captured game screen with blur shader
            batch.setShader(blurShader);
            blurShader.bind();
            blurShader.setUniformf("u_blur_radius", BLUR_RADIUS);

            batch.begin();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.draw(gameScreenTexture, 
                0, 0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight());
            batch.end();
        } else {
            // Update background timer and index
            backgroundTimer += delta;
            if (backgroundTimer >= BACKGROUND_SWITCH_TIME) {
                backgroundTimer = 0;
                currentBackgroundIndex = (currentBackgroundIndex + 1) % backgroundTextures.length;
            }

            // Draw current background image with blur shader
            batch.setShader(blurShader);
            blurShader.bind();
            blurShader.setUniformf("u_blur_radius", BLUR_RADIUS);

            batch.begin();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.draw(backgroundTextures[currentBackgroundIndex], 
                0, 0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight());
            batch.end();
        }

        // Reset shader for UI elements
        batch.setShader(null);

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
        if (stage != null) {
            stage.clear();
        }
        dispose();
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
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (blurShader != null) {
            blurShader.dispose();
            blurShader = null;
        }
        if (gameScreenTexture != null) {
            gameScreenTexture.dispose();
            gameScreenTexture = null;
        }
        if (backgroundTextures != null) {
            for (Texture texture : backgroundTextures) {
                texture.dispose();
            }
            backgroundTextures = null;
        }
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);
            fullscreenLabel.setText("Windowed");
            
            // Update global variable;
            Window.isFullscreen = false;
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            fullscreenLabel.setText("Fullscreen");
            
            // Update global variable;
            Window.isFullscreen = true;
        }
        
        // Clear and rebuild stage after changing screen mode
        stage.clear();
        show();
    }

}
