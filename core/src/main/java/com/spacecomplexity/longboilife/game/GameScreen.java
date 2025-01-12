package com.spacecomplexity.longboilife.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.Main;
import com.spacecomplexity.longboilife.MainInputManager;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainCamera;
import com.spacecomplexity.longboilife.game.globals.MainTimer;
import com.spacecomplexity.longboilife.game.globals.Window;
import com.spacecomplexity.longboilife.game.tile.InvalidSaveMapException;
import com.spacecomplexity.longboilife.game.ui.UIManager;
import com.spacecomplexity.longboilife.game.utils.*;
import com.spacecomplexity.longboilife.game.world.World;

import java.io.FileNotFoundException;


/**
 * Main class to control the game logic.
 */
public class GameScreen implements Screen {
    private final Main game;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private UIManager ui;
    private InputManager inputManager;

    private Viewport viewport;

    private World world;

    private final GameState gameState = GameState.getState();

    public GameScreen(Main game) {
        this.game = game;

        // Initialise SpriteBatch and ShapeRender for rendering
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
    }

    
    public void newGame() {
        gameState.reset();

        // Creates a new World object from "map.json" file
        try {
            world = new World(Gdx.files.internal("map.json"));
        } catch (FileNotFoundException | InvalidSaveMapException e) {
            throw new RuntimeException(e);
        }

        // Create a new timer for 5 minutes
        MainTimer.getTimerManager().getTimer().setTimer(Constants.GAME_LENGTH * 1000);
        MainTimer.getTimerManager().getTimer().setEvent(() -> {
            EventHandler.getEventHandler().callEvent(Events.Event.GAME_END);
        });
        
        resumeGame();
    }
    
    public void resumeGame() {
        // Create an input multiplexer to handle input from all sources
        InputMultiplexer inputMultiplexer = new InputMultiplexer(new MainInputManager());

        // Initialises camera with CameraManager
        CameraManager camera = new CameraManager(world);
        MainCamera.setMainCamera(camera);

        // Initialise viewport for rescaling
        viewport = new ScreenViewport(MainCamera.camera().getCamera());

        // Calculates the scaling factor based initial screen height
        GameUtils.calculateScaling();

        // Initialise UI elements with UIManager
        ui = new UIManager(inputMultiplexer);

        // Position camera in the center of the world map
        MainCamera.camera().position.set(new Vector3(
                world.getWidth() * Constants.TILE_SIZE * gameState.scaleFactor / 2,
                world.getHeight() * Constants.TILE_SIZE * gameState.scaleFactor / 2,
                0));

        // Set up an InputManager to handle user inputs
        inputManager = new InputManager(inputMultiplexer, world);
        // Set the Gdx input processor to handle all our input processes
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Initialise the events performed from this script.
        initialiseEvents();
        
        gameState.paused = false;
        MainTimer.getTimerManager().getTimer().resumeTimer();
    }
    
    /**
     * Responsible for starting and resuming the game.
     */
    @Override
    public void show() {
        // Creates a new game if the game has not started yet
        if (!gameState.active) {
            newGame();
        }
        // Otherwise resume the current game
        else {
            resumeGame();
        }
        
        gameState.active = true;
    }

    /**
     * Initialise events for the event handler.
     */
    private void initialiseEvents() {
        EventHandler eventHandler = EventHandler.getEventHandler();
        eventHandler.initialiseEvents(game, world);
    }

    /**
     * Renders the game world, and make calls to handle continuous inputs.
     * Called every frame.
     */
    @Override
    public void render(float delta) {
        if (!gameState.gameOver) {
            MainTimer.getTimerManager().getTimer().poll();
        }
        
        MainTimer.getTimerManager().getTimer().pollGameEvents();
        
        // Call to handles any constant input
        inputManager.handleContinuousInput();

        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1f);

        // Applies viewport transformations and updates camera ready for rendering
        viewport.apply();
        MainCamera.camera().update();
        // Update the SpriteBatch and ShapeRenderer to match the updates camera
        batch.setProjectionMatrix(MainCamera.camera().getCombinedMatrix());
        shapeRenderer.setProjectionMatrix(MainCamera.camera().getCombinedMatrix());

        // Darkened the world when paused
        Color worldTint = gameState.paused ? Color.LIGHT_GRAY : Color.WHITE;

        // Draw the world tiles
        RenderUtils.drawWorld(batch, world, worldTint);
        // Draw the worlds buildings
        RenderUtils.drawBuildings(batch, world, worldTint);
        // If there is a building to be placed draw it as a ghost building
        if (gameState.placingBuilding != null) {
            RenderUtils.drawPlacingBuilding(batch, world, gameState.placingBuilding, new Color(1f, 1f, 1f, 0.75f), new Color(1f, 0f, 0f, 0.75f));
        }
        // If we are placing a building or there is a building selected then draw gridlines
        if (gameState.placingBuilding != null || gameState.selectedBuilding != null) {
            RenderUtils.drawWorldGridlines(shapeRenderer, world, Color.BLACK);
        }
        // If there is a building selected then outline it
        if (gameState.selectedBuilding != null) {
            RenderUtils.outlineBuilding(shapeRenderer, gameState.selectedBuilding, Color.RED, 2);
        }
        // If there is a moving selected then outline where it was previously
        if (gameState.movingBuilding != null) {
            RenderUtils.outlineBuilding(shapeRenderer, gameState.movingBuilding, Color.PURPLE, 2);
        }

        // Render the UI
        ui.render();

        if (!gameState.paused && gameState.active) {
            // Update profit timer
            gameState.updateProfitTimer(delta);
            
            // Check if 5 seconds have passed
            if (gameState.getProfitTimer() >= GameState.PROFIT_INTERVAL) {
                // Calculate profit from all buildings
                float totalProfit = 0;
                for (BuildingType type : BuildingType.values()) {
                    int count = gameState.getBuildingCount(type);
                    totalProfit += count * type.getProfitPerTick();
                }
                
                // Add profit to money
                gameState.money += totalProfit;
                
                // Reset timer
                gameState.updateProfitTimer(-GameState.PROFIT_INTERVAL);
            }
        }
    }

    /**
     * Handles resizing events, to ensure the game can be scaled.
     * Called when the game window is resized.
     *
     * @param width  the new width in pixels.
     * @param height the new height in pixels.
     */
    @Override
    public void resize(int width, int height) {
        // Updates the window width/height globals
        Window.update(width, height);

        // Updates viewport to match new window size
        viewport.update(width, height, false);

        // Recalculate scaling factors with new height
        GameUtils.calculateScaling();

        // Rescale UI
        ui.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // Ensure UI is disposed before hiding screen
        if (ui != null) {
            ui.dispose();
        }
        
        // Clear input processor to prevent ghost inputs
        Gdx.input.setInputProcessor(null);
    }

    /**
     * Release all resources held by the game.
     * Called when the game is being closed.
     */
    @Override
    public void dispose() {
        // Dispose of all resources in reverse order of creation
        if (ui != null) {
            ui.dispose();
            ui = null;
        }
        
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        
        if (world != null) {
            world = null;
        }
    }

    /**
     * Renders the game world without UI elements for screen capture.
     */
    public void renderWorldOnly() {
        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1f);

        // Applies viewport transformations and updates camera ready for rendering
        viewport.apply();
        MainCamera.camera().update();
        // Update the SpriteBatch and ShapeRenderer to match the updates camera
        batch.setProjectionMatrix(MainCamera.camera().getCombinedMatrix());
        shapeRenderer.setProjectionMatrix(MainCamera.camera().getCombinedMatrix());

        // Darkened the world when paused
        Color worldTint = gameState.paused ? Color.LIGHT_GRAY : Color.WHITE;

        // Draw the world tiles
        RenderUtils.drawWorld(batch, world, worldTint);
        // Draw the worlds buildings
        RenderUtils.drawBuildings(batch, world, worldTint);
    }

    public UIManager getUIManager() {
        return ui;
    }
}
