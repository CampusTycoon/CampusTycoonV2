package com.spacecomplexity.longboilife.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainCamera;
import com.spacecomplexity.longboilife.game.globals.Window;
import com.spacecomplexity.longboilife.game.world.World;

/**
 * A class used for game utilities.
 */
public class GameUtils {

    /**
     * Get the current position of the mouse relative to the world grid.
     *
     * @param world the world reference for size.
     * @return the grid index at the current mouse position.
     */
    public static Vector2Int getMouseOnGrid(World world) {
        // Get the position of mouse in world coordinates
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        MainCamera.camera().getCamera().unproject(mouse);

        // Divide these by the cell size (as the world starts at (0, 0))
        float cellSize = Constants.TILE_SIZE * GameState.getState().scaleFactor;
        return new Vector2Int(
            (int) (mouse.x / cellSize),
            (int) (mouse.y / cellSize)
        );
    }

    /**
     * Calculate and set scaling factors using the window size.
     */
    public static void calculateScaling() {
        int screenHeight = Window.height;

        // If height is 0 then the window is minimised so don't bother calculating as this could cause unintended behaviour with scaling at 0
        if (screenHeight == 0)
            return;

        // Calculate scale factor based on screen height linearly using constant
        GameState.getState().scaleFactor = screenHeight / (float) Constants.SCALING_1_HEIGHT;
        // Calculate UI scale factor based on screen height using scaling map
        GameState.getState().uiScaleFactor = Constants.UI_SCALING_MAP.floorEntry(screenHeight).getValue();
    }
}
