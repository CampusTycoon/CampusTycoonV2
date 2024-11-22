package com.spacecomplexity.longboilife.game.building;

import com.spacecomplexity.longboilife.game.utils.Vector2Int;

/**
 * Represents a building in the game.
 */
public class Building {
    private final BuildingType type;
    private Vector2Int position;

    /**
     * Constructs a building instance given the specific type.
     *
     * @param type     the type of building to create.
     * @param position the position of this building in the world.
     */
    public Building(BuildingType type, Vector2Int position) {
        this.type = type;
        this.position = position;
    }

    public BuildingType getType() {
        return type;
    }

    public Vector2Int getPosition() {
        return position;
    }

    public void setPosition(Vector2Int position) {
        this.position = position;
    }
}
