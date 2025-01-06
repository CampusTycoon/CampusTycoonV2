package com.spacecomplexity.longboilife.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector3;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.Keybindings;
import com.spacecomplexity.longboilife.game.globals.MainCamera;
import com.spacecomplexity.longboilife.game.tile.Tile;
import com.spacecomplexity.longboilife.game.world.World;

public class InputManager {
    private final GameState gameState = GameState.getState();
    private final World world;

    /**
     * Create an input manager by initialising the input processors and set attributes.
     *
     * @param inputMultiplexer to add the input processor events to the input processing.
     */
    public InputManager(InputMultiplexer inputMultiplexer, World world) {
        this.world = world;
        inputMultiplexer.addProcessor(new InputProcessor());
    }

    /**
     * Handles continuous input which won't be processed in {@link InputManager.InputProcessor} (as this is event driven).
     * Should be called every frame (before rendering).
     */
    public void handleContinuousInput() {
        // If game is over, don't process any continuous input
        if (gameState.gameOver) {
            return;
        }

        float deltaTime = Gdx.graphics.getDeltaTime();

        // Calculate camera speed and move camera around given camera direction keys pressed
        float cameraSpeed = gameState.cameraSpeed * deltaTime * MainCamera.camera().zoom * gameState.scaleFactor;
        if (Gdx.input.isKeyPressed(Keybindings.CAMERA_UP.getKey())) {
            MainCamera.camera().position.y += cameraSpeed;
        }
        if (Gdx.input.isKeyPressed(Keybindings.CAMERA_DOWN.getKey())) {
            MainCamera.camera().position.y -= cameraSpeed;
        }
        if (Gdx.input.isKeyPressed(Keybindings.CAMERA_LEFT.getKey())) {
            MainCamera.camera().position.x -= cameraSpeed;
        }
        if (Gdx.input.isKeyPressed(Keybindings.CAMERA_RIGHT.getKey())) {
            MainCamera.camera().position.x += cameraSpeed;
        }

        // Calculate camera zoom speed and zoom camera around given camera zoom keys pressed
        float cameraZoomSpeed = gameState.cameraKeyZoomSpeed * deltaTime * MainCamera.camera().zoom;
        if (Gdx.input.isKeyPressed(Keybindings.CAMERA_ZOOM_IN.getKey())) {
            MainCamera.camera().zoom += cameraZoomSpeed;
        }
        if (Gdx.input.isKeyPressed(Keybindings.CAMERA_ZOOM_OUT.getKey())) {
            MainCamera.camera().zoom -= cameraZoomSpeed;
        }
    }

    /**
     * Handles input (event driven).
     * Will be called every frame (before rendering, hence before {@link InputManager#handleContinuousInput()}).
     */
    private class InputProcessor extends InputAdapter {
        private final EventHandler eventHandler = EventHandler.getEventHandler();

        /**
         * Zoom the camera at the mouse position when the scroll wheel/trackpad is used.
         *
         * @param amountX the horizontal scroll amount, negative or positive depending on the direction the wheel was scrolled.
         * @param amountY the vertical scroll amount, negative or positive depending on the direction the wheel was scrolled.
         * @return true to show the event was handled.
         */
        @Override
        public boolean scrolled(float amountX, float amountY) {
            // If game is over, don't allow zooming
            if (gameState.gameOver) {
                return true;
            }
            
            float deltaTime = Gdx.graphics.getDeltaTime();

            // Convert the current mouse position into world coordinates
            Vector3 mousePosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            MainCamera.camera().getCamera().unproject(mousePosition);

            // Zoom in/out at the mouses current position
            MainCamera.camera().zoomAt(amountY * gameState.cameraScrollZoomSpeed * deltaTime, mousePosition);

            return false;
        }

        private int lastButton;

        // Used to store mouse position between dragging frames
        private float lastScreenX, lastScreenY;

        /**
         * Mouse down event.
         *
         * @param screenX The x coordinate, origin is in the upper left corner.
         * @param screenY The y coordinate, origin is in the upper left corner.
         * @param pointer the pointer for the event.
         * @param button  the button pressed.
         * @return true to show the event was handled.
         */
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Set the last button so we can check what to do in the touchDragged event
            lastButton = button;

            switch (button) {
                // If main button clicked
                case 0:
                    // If game is paused or over don't allow any actions
                    if (gameState.paused || gameState.gameOver) {
                        return true;
                    }

                    // If a building is selected then try to build this
                    if (GameState.getState().placingBuilding != null) {
                        eventHandler.callEvent(EventHandler.Event.BUILD);
                    }

                    // Else try and select a building already on the map
                    else {
                        eventHandler.callEvent(EventHandler.Event.SELECT_BUILDING);
                    }

                    break;

                // If secondary or tertiary button clicked
                case 1:
                case 2:
                    // Record the initial touch position for the drag event
                    lastScreenX = screenX;
                    lastScreenY = screenY;

                    break;
            }

            return true;
        }

        /**
         * Mouse down (not initial frame) event.
         *
         * @param screenX The x coordinate, origin is in the upper left corner.
         * @param screenY The y coordinate, origin is in the upper left corner.
         * @param pointer the pointer for the event.
         * @return true to show the event was handled.
         */
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            // If game is over, don't allow any interactions
            if (gameState.gameOver) {
                return true;
            }

            switch (lastButton) {
                // If main button clicked
                case 0:
                    // If game is paused don't allow any actions
                    if (gameState.paused) {
                        return true;
                    }

                    // If a building is selected then try to build
                    // This allows drag placing buildings which are not automatically deselected
                    if (GameState.getState().placingBuilding != null) {
                        eventHandler.callEvent(EventHandler.Event.BUILD);
                    }

                    break;
                // If secondary or tertiary button clicked
                case 1:
                case 2:
                    // Calculate the difference between last mouse position and now
                    float deltaX = screenX - lastScreenX;
                    float deltaY = screenY - lastScreenY;

                    // Move the camera the respective amount to simulate dragging
                    MainCamera.camera().position.x -= deltaX * MainCamera.camera().zoom;
                    MainCamera.camera().position.y += deltaY * MainCamera.camera().zoom;

                    lastScreenX = screenX;
                    lastScreenY = screenY;
                    break;
            }

            return true;
        }

        /**
         * Handles onKeyPress events.
         *
         * @param keycode one of the constants in {@link com.badlogic.gdx.Input.Keys}
         * @return true if the event was handled.
         */
        @Override
        public boolean keyDown(int keycode) {
            // If the close key is pressed, send events to cancel actions
            if (keycode == Keybindings.CANCEL.getKey()) {
                eventHandler.callEvent(EventHandler.Event.CANCEL_OPERATIONS);

                return true;
            }

            // If the pause key is pressed, pause/resume the game
            else if (keycode == Keybindings.PAUSE.getKey()) {
                // If the game is over don't allow pause
                if (gameState.gameOver) {
                    return true;
                }

                eventHandler.callEvent(GameState.getState().paused ? EventHandler.Event.RESUME_GAME : EventHandler.Event.PAUSE_GAME);

                return true;
            }

            return false;
        }

        /**
         * Handles mouseMoved events.
         *
         * @param screenX The x coordinate, origin is in the upper left corner.
         * @param screenY The y coordinate, origin is in the upper left corner.
         * @return true if the event was handled.
         */
        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            // If game is paused or over, don't show tooltips
            if (gameState.paused || gameState.gameOver) {
                gameState.hoveredBuilding = null;
                return true;
            }

            // Get the tile at mouse position
            Tile tile = world.getTile(GameUtils.getMouseOnGrid(world));
            if (tile != null) {
                gameState.hoveredBuilding = tile.getBuildingRef();
            } else {
                gameState.hoveredBuilding = null;
            }
            
            return true;
        }
    }
}
