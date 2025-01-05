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
import com.spacecomplexity.longboilife.achievements.AchievementManager;
import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainCamera;
import com.spacecomplexity.longboilife.game.globals.MainTimer;
import com.spacecomplexity.longboilife.game.globals.Window;
import com.spacecomplexity.longboilife.game.tile.InvalidSaveMapException;
import com.spacecomplexity.longboilife.game.tile.Tile;
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
        MainTimer.getTimerManager().getTimer().setTimer(1 * 300 * 1000);
        MainTimer.getTimerManager().getTimer().setEvent(() -> {
            EventHandler.getEventHandler().callEvent(EventHandler.Event.GAME_END);
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
        inputManager = new InputManager(inputMultiplexer);
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

        // Build the selected building
        eventHandler.createEvent(EventHandler.Event.BUILD, (params) -> {
            BuildingType toBuild = gameState.placingBuilding;

            // If there is no selected building do nothing
            if (toBuild == null) {
                return null;
            }

            // If the building is in an invalid location then don't built
            Vector2Int mouse = GameUtils.getMouseOnGrid(world);
            if (!world.canBuild(toBuild, mouse)) {
                return null;
            }

            // If there is no moving building then this is a new build
            if (gameState.movingBuilding == null) {
                // If the user doesn't have enough money to buy the building then don't build
                float cost = toBuild.getCost();
                if (gameState.money < cost) {
                    return null;
                }

                // Build the building at the mouse location and charge the player accordingly
                world.build(toBuild, mouse);
                gameState.money -= cost;

                // Check for achievements
                AchievementManager.getInstance().checkAchievements();

                // Remove the selected building if shift is not held
                if (!gameState.shiftHeld) {
                    gameState.placingBuilding = null;
                }
            }
            // If there is a moving building then this is a moved building.
            else {
                // If the user doesn't have enough money to buy the building then don't build
                float cost = toBuild.getCost() * Constants.moveCostRecovery;
                if (gameState.money < cost) {
                    return null;
                }

                // Build the building at the mouse location and charge the player accordingly
                world.build(gameState.movingBuilding, mouse);
                gameState.money -= cost;

                // Remove the old moving building and selected building
                gameState.movingBuilding = null;
                gameState.placingBuilding = null;
            }

            return null;
        });

        // Select a previously built building
        eventHandler.createEvent(EventHandler.Event.SELECT_BUILDING, (params) -> {
            // Get the tile at the mouse coordinates
            Tile tile = world.getTile(GameUtils.getMouseOnGrid(world));
            // If there is no tile here then do nothing
            if (tile == null) {
                return null;
            }
            // Get the building on the tile
            Building selectedBuilding = tile.getBuildingRef();
        
            // If there is no building here then do nothing
            if (selectedBuilding == null) {
                return null;
            }
        
            // Set the selected building
            gameState.selectedBuilding = selectedBuilding;
        
            // Open the selected building menu
            eventHandler.callEvent(EventHandler.Event.OPEN_SELECTED_MENU);
        
            return null;
        });

        // Cancel all events
        eventHandler.createEvent(EventHandler.Event.CANCEL_OPERATIONS, (params) -> {
            // Close menus and deselect any buildings
            eventHandler.callEvent(EventHandler.Event.CLOSE_BUILD_MENU);
            gameState.placingBuilding = null;
            eventHandler.callEvent(EventHandler.Event.CLOSE_SELECTED_MENU);
            gameState.selectedBuilding = null;

            // If there is a building move in progress cancel this
            if (gameState.movingBuilding != null) {
                world.build(gameState.movingBuilding);
                gameState.movingBuilding = null;
            }

            return null;
        });

        // Sell the selected building
        eventHandler.createEvent(EventHandler.Event.SELL_BUILDING, (params) -> {
            // Delete the building
            world.demolish(gameState.selectedBuilding);
            // Refund the specified amount
            gameState.money += gameState.selectedBuilding.getType().getCost() * Constants.sellCostRecovery;
            // Deselect the removed building
            gameState.selectedBuilding = null;

            return null;
        });

        // Start the move of the selected building
        eventHandler.createEvent(EventHandler.Event.MOVE_BUILDING, (params) -> {
            float cost = gameState.selectedBuilding.getType().getCost() * Constants.moveCostRecovery;
            // If we don't have enough money then don't allow the move
            if (gameState.money < cost) {
                return null;
            }

            // Delete the original building
            world.demolish(gameState.selectedBuilding);
            // Select the same type of building to be placed again
            gameState.placingBuilding = gameState.selectedBuilding.getType();
            // Deselect the removed building and set it to the building to be moved
            gameState.movingBuilding = gameState.selectedBuilding;
            gameState.selectedBuilding = null;

            // Close the menu
            eventHandler.callEvent(EventHandler.Event.CLOSE_SELECTED_MENU);

            return null;
        });

        // Return to the menu
        eventHandler.createEvent(EventHandler.Event.RETURN_MENU, (params) -> {
            game.switchScreen(Main.ScreenType.MENU);

            return null;
        });

        // Open settings menu
        eventHandler.createEvent(EventHandler.Event.OPEN_SETTINGS, (params) -> {
            game.openSettings(Main.ScreenType.GAME);
            return null;
        });
    }

    /**
     * Renders the game world, and make calls to handle continuous inputs.
     * Called every frame.
     */
    @Override
    public void render(float delta) {
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

        // Poll the timer to run the event if the timer has expired
        // Do not update satisfaction score if the game is paused or has ended
        if (!gameState.paused && !MainTimer.getTimerManager().getTimer().poll()) {
            // Update the satisfaction score
            Satisfaction.updateSatisfactionScore(world);
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
        ui.dispose();
    }

    /**
     * Release all resources held by the game.
     * Called when the game is being closed.
     */
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        ui.dispose();
    }
}
