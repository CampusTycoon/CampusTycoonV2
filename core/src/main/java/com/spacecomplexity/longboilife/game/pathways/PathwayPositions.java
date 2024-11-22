package com.spacecomplexity.longboilife.game.pathways;

/**
 * Enum containing information on how to draw pathways.
 */
public enum PathwayPositions {
    TOP_BOTTOM(0, PathwayTextures.Type.STRAIGHT),
    LEFT_RIGHT(90, PathwayTextures.Type.STRAIGHT),

    LEFT_TOP(0, PathwayTextures.Type.CORNER),
    BOTTOM_LEFT(90, PathwayTextures.Type.CORNER),
    RIGHT_BOTTOM(180, PathwayTextures.Type.CORNER),
    TOP_RIGHT(270, PathwayTextures.Type.CORNER),

    LEFT_TOP_RIGHT(0, PathwayTextures.Type.TJUNC),
    BOTTOM_LEFT_TOP(90, PathwayTextures.Type.TJUNC),
    RIGHT_BOTTOM_LEFT(180, PathwayTextures.Type.TJUNC),
    TOP_RIGHT_BOTTOM(270, PathwayTextures.Type.TJUNC),

    TOP_LEFT_BOTTOM_RIGHT(0, PathwayTextures.Type.CROSS),
    ;

    private final float rotation;
    private final PathwayTextures.Type textureType;

    private PathwayPositions(float rotation, PathwayTextures.Type textureType) {
        this.rotation = rotation;
        this.textureType = textureType;
    }

    public float getRotation() {
        return rotation;
    }

    public PathwayTextures.Type getTextureType() {
        return textureType;
    }
}
