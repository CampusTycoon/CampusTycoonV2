package com.spacecomplexity.longboilife.game.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.world.World;

/**
 * Manages the camera for rendering the game world.
 */
public class CameraManager {
    private final OrthographicCamera camera;
    public World world;
    private final GameState gameState = GameState.getState();
    public Vector3 position;
    public float zoom;

    /**
     * Creates a camera with specified world for clamping.
     *
     * @param world the current world to get dimensions from.
     */
    public CameraManager(World world) {
        camera = new OrthographicCamera();
        this.world = world;
        position = new Vector3();
        zoom = 1;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * Clamps the camera position and zoom to keep it within the specified bounds then calls {@link Camera#update()} to recalculate the cameras matrix.
     */
    public void update() {
        // Clamp the position within the specified bounds to keep it from going over 50% of the screen off the map in any direction
        position.set(new Vector3(
            MathUtils.clamp(position.x, 0, world.getWidth() * Constants.TILE_SIZE * gameState.scaleFactor),
            MathUtils.clamp(position.y, 0, world.getHeight() * Constants.TILE_SIZE * gameState.scaleFactor),
            0
        ));
        camera.position.set(position);

        // Clamp the zoom within the specified bounds
        zoom = MathUtils.clamp(zoom, Constants.MIN_ZOOM, Constants.MAX_ZOOM);
        camera.zoom = zoom;

        // Update camera with its new position and zoom levels
        camera.update();
    }

    /**
     * Get the combined projection and view matrix from the camera.
     *
     * @return the combined projection and view matrix.
     */
    public Matrix4 getCombinedMatrix() {
        return camera.combined;
    }

    /**
     * Zoom the camera at a specific location instead of the center of screen.
     *
     * @param change     the amount to change the zoom level by.
     * @param atPosition the position to zoom the camera at in world coordinates.
     */
    public void zoomAt(float change, Vector3 atPosition) {
        // Calculate what the new zoom value will be
        float newZoom = MathUtils.clamp(zoom + change, Constants.MIN_ZOOM, Constants.MAX_ZOOM);

        // Adjust the camera position based on the zoom factor and the desired zoom position.
        // This moves the camera towards the specified position (`atPosition`) while compensating for the change in zoom level.
        float zoomFactor = newZoom / zoom;
        position.x += (atPosition.x - position.x) * (1 - zoomFactor);
        position.y += (atPosition.y - position.y) * (1 - zoomFactor);

        zoom = newZoom;
    }
}
