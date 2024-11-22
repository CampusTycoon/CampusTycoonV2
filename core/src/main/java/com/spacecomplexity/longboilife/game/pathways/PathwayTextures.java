package com.spacecomplexity.longboilife.game.pathways;

import com.badlogic.gdx.graphics.Texture;
import com.spacecomplexity.longboilife.game.building.BuildingType;

import java.util.HashMap;

/**
 * Class to get the texture for a specific pathway building and texture type.
 */
public class PathwayTextures {
    /**
     * Type of pathway connections.
     */
    public enum Type {
        STRAIGHT,
        CORNER,
        TJUNC,
        CROSS,
        ;
    }

    /**
     * Map containing the textures
     */
    private static final HashMap<BuildingType, HashMap<PathwayTextures.Type, Texture>> textureList = new HashMap<>() {{
        put(BuildingType.ROAD, new HashMap<>() {{
            put(PathwayTextures.Type.STRAIGHT, new Texture("buildings/roads/straight.png"));
            put(PathwayTextures.Type.CORNER, new Texture("buildings/roads/corner.png"));
            put(PathwayTextures.Type.TJUNC, new Texture("buildings/roads/3-way.png"));
            put(PathwayTextures.Type.CROSS, new Texture("buildings/roads/4-way.png"));
        }});
    }};

    /**
     * Get the texture for a pathway tile.
     *
     * @param buildingType       the building type.
     * @param pathwayTextureType the type of texture.
     * @return the texture fitting these criteria.
     */
    public static Texture getTexture(BuildingType buildingType, PathwayTextures.Type pathwayTextureType) {
        return textureList.get(buildingType).get(pathwayTextureType);
    }
}
