package com.spacecomplexity.longboilife.game.globals;

import com.spacecomplexity.longboilife.game.building.BuildingCategory;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Class contain the constants used throughout the game.
 */
public class Constants {
    /**
     * The size of tiles (in px).
     */
    public static final int TILE_SIZE = 8;

    /**
     * The height of the window at scaling level 1.
     */
    public static final int SCALING_1_HEIGHT = 1080;

    /**
     * Map of the UI scaling at the specified window height.
     */
    public static final TreeMap<Integer, Float> UI_SCALING_MAP = new TreeMap<>() {{
        put(0, 1f);
        put(720, 1.5f);
        put(1440, 2f);
        put(2160, 2.5f);
    }};

    /**
     * Minimum camera zoom level.
     */
    public static final float MIN_ZOOM = 0.05f;

    /**
     * Maximum camera zoom level.
     */
    public static final float MAX_ZOOM = 0.5f;

    /**
     * The number of seconds each game lasts for.
     */
    public static final int GAME_LENGTH = 300;

    /**
     * The rate at which events occur compared to normal (1.0)
     * Setting it to 0.8 should mean that there are 2.4 expected events per game.
     */
    public static final double EVENT_FREQUENCY = 0.8;

    /**
     * The proportion of money the user will get back form selling the building.
     */
    public static float sellCostRecovery = 0.5f;

    /**
     * The proportion of money the user will need in order to move the building.
     */
    public static float moveCostRecovery = 0.25f;

    /**
     * The satisfactory distances from accommodation to a specific category of building
     * <p>
     * Used for calculating satisfaction score
     */
    public static HashMap<BuildingCategory, Float> satisfactoryDistance = new HashMap<BuildingCategory, Float>() {{
        put(BuildingCategory.FOOD, 5f);
        put(BuildingCategory.RECREATIONAL, 5f);
        put(BuildingCategory.EDUCATIONAL, 10f);
    }};
}
