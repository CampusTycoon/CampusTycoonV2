package com.spacecomplexity.longboilife.game.tile;

import com.badlogic.gdx.graphics.Texture;

/**
 * Contains a list of all tile types, including there default data.
 */
public enum TileType {
    GRASS(new Texture("tiles/grass.png"), true),
    WATER(new Texture("tiles/water.png"), false),
    ;

    private final Texture texture;
    private final boolean isNaturallyBuildable;

    /**
     * Constructor to create a {@link TileType} with specified attributes.
     *
     * @param texture              the texture representing the tile.
     * @param isNaturallyBuildable a boolean indicating if the tile can be built on.
     */
    TileType(Texture texture, boolean isNaturallyBuildable) {
        this.texture = texture;
        this.isNaturallyBuildable = isNaturallyBuildable;
    }

    public Texture getTexture() {
        return texture;
    }

    /**
     * Get whether this type of tile is allowed to be built on.
     *
     * @return whether this type of tile is allowed to be built on.
     */
    public boolean isNaturallyBuildable() {
        return isNaturallyBuildable;
    }

    /**
     * Will dispose of the all loaded assets (like textures).
     * <p>
     * <strong>Warning:</strong> Once disposed of no attributes will be able to be reloaded, which could lead to undefined behaviour.
     */
    public void dispose() {
        texture.dispose();
    }
}
