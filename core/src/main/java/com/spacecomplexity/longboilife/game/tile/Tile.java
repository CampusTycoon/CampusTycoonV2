package com.spacecomplexity.longboilife.game.tile;

import com.spacecomplexity.longboilife.game.building.Building;

/**
 * Represents a tile in the game.
 */
public class Tile {
    private final TileType type;
    private boolean isBuildable;
    private Building buildingRef = null;

    /**
     * Constructs a tile instance given the specific tile.
     *
     * @param type the type of tile to create.
     */
    public Tile(TileType type) {
        this.type = type;
        isBuildable = type.isNaturallyBuildable();
    }

    public TileType getType() {
        return type;
    }

    /**
     * Get whether the tile is currently able to be built on.
     *
     * @return whether the tile is buildable.
     */
    public boolean isBuildable() {
        return isBuildable;
    }

    /**
     * Set the buildable status of this tile.
     *
     * @param isBuildable the new buildable status.
     */
    public void setBuildable(boolean isBuildable) {
        this.isBuildable = isBuildable;
    }

    /**
     * Get the reference to building which is currently on this tile.
     *
     * @return the reference to the  building on this tile.
     */
    public Building getBuildingRef() {
        return buildingRef;
    }

    /**
     * Set the reference to building which is currently on this tile.
     *
     * @param buildingRef the new building ref.
     */
    public void setBuildingRef(Building buildingRef) {
        this.buildingRef = buildingRef;
    }
}
