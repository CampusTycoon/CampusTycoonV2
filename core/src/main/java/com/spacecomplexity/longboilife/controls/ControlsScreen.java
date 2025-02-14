package com.spacecomplexity.longboilife.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
import com.badlogic.gdx.Input.Keys;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.badlogic.gdx.ScreenAdapter;

/**
 * Main class to control the controls screen.
 */
public class ControlsScreen extends ScreenAdapter {
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

    private ShaderProgram blurShader;
    private static final float BLUR_RADIUS = 2.5f;

    // Hard-coded control entries
    private static final String[][] CONTROL_ENTRIES = {
        {"Move Camera", "W/A/S/D"},
        {"Zoom Camera", "Mouse Wheel/Q/E"},
        {"Cancel Selection", "ESC"},
        {"Open Menu", "ESC"},
        {"Toggle Fullscreen", "F11"},
        {"Pause Game", "Space"},
        {"Place Multiple Buildings", "SHIFT"}
    };

    public ControlsScreen(Main game, Main.ScreenType previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        // Initialise viewport and drawing elements
        viewport = new ScalingViewport(Scaling.fit, Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

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

        // Load background textures
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
        // Clear any existing actors before setting up new ones
        stage.clear();

        // Create a custom input processor that handles ESC key
        InputMultiplexer inputMultiplexer = new InputMultiplexer(
            new MainInputManager() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Keys.ESCAPE) {
                        // If we came from game screen and the game is over, go to menu
                        if (previousScreen == Main.ScreenType.GAME && GameState.getState().gameOver) {
                            game.switchScreen(Main.ScreenType.MENU);
                        } else {
                            // Otherwise, go back to previous screen
                            game.switchScreen(previousScreen);
                        }
                        return true;
                    }
                    return super.keyDown(keycode);
                }
            }, 
            stage
        );
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Create main container table with background
        Table containerTable = new Table();
        containerTable.setFillParent(true);
        containerTable.center(); // Center the content
        stage.addActor(containerTable);

        // Create content table for control entries
        Table contentTable = new Table();
        contentTable.setSkin(skin);
        contentTable.setBackground(skin.getDrawable("panel1")); // Add background to the content table

        // Create a table to display one row of labels
        Table labelsRow = new Table();
        labelsRow.setSkin(skin);
        
        // Create header buttons with equal width and spacing between them
        TextButton actionButton = new TextButton("Action", skin, "round");
        TextButton keysButton = new TextButton("Keys", skin, "round");
        
        // Add buttons to the row with spacing
        labelsRow.add(actionButton).width(200).padRight(100);  // Fixed equal width with padding between
        labelsRow.add(keysButton).width(200);                   // Same fixed width
        
        // Add labels to content table with some extra padding at the bottom
        contentTable.add(labelsRow).expandX().fillX().pad(20).padBottom(30);
        contentTable.row();

        // Add control entries to the table
        for (String[] entry : CONTROL_ENTRIES) {
            Table row = new Table();
            // Create a container for action that matches the header button width
            Table actionCell = new Table();
            actionCell.add(new Label(entry[0], skin)).center();
            
            // Create a container for keys that matches the header button width
            Table keysCell = new Table();
            keysCell.add(new Label(entry[1], skin)).center();
            
            // Add the cells with the same widths and spacing as the headers
            row.add(actionCell).width(200).padRight(100);
            row.add(keysCell).width(200);
            
            contentTable.add(row).expandX().fillX().pad(10);
            contentTable.row();
        }

        // Add extra padding after the control entries
        contentTable.add().pad(10);

        // Add the content table to the container with some padding
        containerTable.add(contentTable).width(600).pad(50);
        containerTable.row();

        // Initialise back button
        TextButton backButton = new TextButton("Back", skin, "round");
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // If we came from game screen and the game is over, go to menu
                if (previousScreen == Main.ScreenType.GAME && GameState.getState().gameOver) {
                    game.switchScreen(Main.ScreenType.MENU);
                } else {
                    // Otherwise, go back to previous screen
                    game.switchScreen(previousScreen);
                }
            }
        });

        // Add buttons to table with bottom alignment
        containerTable.row();  // Move to next row
        containerTable.add(backButton).colspan(2).padBottom(10);  // colspan(2) makes button span both columns
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1f);

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
        if (blurShader != null) {
            blurShader.dispose();
        }
    }
}
