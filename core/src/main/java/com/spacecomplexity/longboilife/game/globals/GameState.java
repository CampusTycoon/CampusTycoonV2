package com.spacecomplexity.longboilife.game.globals;

import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.building.BuildingType;

import java.util.HashMap;

/**
 * Singleton class to contain variables relating to state/config of the game.
 */
public class GameState {
    private static final GameState gameState = new GameState();
    /**
     * Whether the game has been started (and has not yet ended).
     */
    public boolean active = false;

    /**
     * The current scale factor of game for rendering.
     */
    public float scaleFactor = 1;

    /**
     * The current scale factor of the UI.
     */
    public float uiScaleFactor = 1;

    /**
     * Camera speed whilst controlling with keyboard.
     */
    public float cameraSpeed;

    /**
     * Camera zooming speed whilst controlling with keyboard.
     */
    public float cameraKeyZoomSpeed;

    /**
     * Camera zooming speed whilst controlling with mouse/trackpad.
     */
    public float cameraScrollZoomSpeed;

    /**
     * The amount of money the player currently has.
     */
    public float money;

    /**
     * The current satisfaction score.
     */
    public double satisfactionScore;

    /**
     * The building selected to be placed.
     * <p>
     * If {@code null} then nothing is selected.
     */
    public BuildingType placingBuilding;

    /**
     * The currently selected building on the map.
     * <p>
     * If {@code null} then nothing is selected.
     */
    public Building selectedBuilding;

    /**
     * The currently selected building to be moved.
     * <p>
     * If {@code null} then nothing is selected.
     */
    public Building movingBuilding;

    /**
     * If the building menu is currently open.
     */
    public boolean buildMenuOpen;

    /**
     * If the game is currently paused.
     */
    public boolean paused;

    /**
     * The current count of buildings.
     * <p>
     * This should be modified by {@link GameState#getBuildingCount(BuildingType)} and {@link GameState#changeBuildingCount(BuildingType, int)} not directly.
     * <p>
     * This is initialised in the constructor.
     */
    public HashMap<BuildingType, Integer> buildingsCount;

    /**
     * Helper function to get the number of a specified building.
     *
     * @param buildingType the building type to get.
     * @return the building count for this specific building.
     */
    public Integer getBuildingCount(BuildingType buildingType) {
        Integer count = buildingsCount.get(buildingType);
        // If this has not yet been set return 0
        if (count == null) {
            return 0;
        }

        return count;
    }

    /**
     * Helper function to change the number of a specified building.
     *
     * @param buildingType the building type to change.
     * @param change       the amount to change it by.
     */
    public void changeBuildingCount(BuildingType buildingType, int change) {
        int count = getBuildingCount(buildingType);
        buildingsCount.put(buildingType, count + change);
    }

    /**
     * If the game has ended
     */
    public boolean gameOver = false;

    /**
     * If the SHIFT key is currently held.
     */
    public boolean shiftHeld = false;

    /**
     * If the building is currently hovered over.
     */
    public float buildingHoverTime = 0f;
    public Building hoveredBuilding = null;

    private float profitTimer = 0f;
    public static final float PROFIT_INTERVAL = 5f; // 5 seconds

    /**
     * Get the singleton instance of the {@link GameState} class.
     *
     * @return The single {@link GameState} class.
     */
    public static GameState getState() {
        return gameState;
    }

    private GameState() {
        reset();
    }

    /**
     * Reset all values to default.
     */
    public void reset() {
        active = false;
//        scaleFactor = 1;
//        uiScaleFactor = 1;
        cameraSpeed = 1400;
        cameraKeyZoomSpeed = 3;
        cameraScrollZoomSpeed = 4;
//        fullscreen = false;
        money = 800000;
        satisfactionScore = 0.0;
        placingBuilding = null;
        selectedBuilding = null;
        movingBuilding = null;
        buildMenuOpen = false;
        paused = false;
        buildingsCount = new HashMap<>();
        gameOver = false;
        shiftHeld = false;
        buildingHoverTime = 0f;
        hoveredBuilding = null;
        profitTimer = 0f;
    }

    public float calculateTotalProfit() {
        float totalProfit = 0;
        for (BuildingType type : BuildingType.values()) {
            int count = getBuildingCount(type);
            totalProfit += count * type.getProfitPerTick();
        }
        return totalProfit;
    }

    public float getProfitTimer() {
        return profitTimer;
    }

    public void updateProfitTimer(float delta) {
        profitTimer += delta;
    }
}
