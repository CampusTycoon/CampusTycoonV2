package com.spacecomplexity.longboilife.game.building;

import com.badlogic.gdx.graphics.Texture;
import com.spacecomplexity.longboilife.game.utils.Vector2Int;

import java.util.stream.Stream;

/**
 * Contains a list of all buildings, including their default data.
 */
public enum BuildingType {
    // Roads
    ROAD("Road", new Texture("buildings/roads/straight.png"), new Vector2Int(1, 1), BuildingCategory.PATHWAY, 100),
    // Accommodation
    BAND1("Band 1 Accommodation", new Texture("buildings/Band1.png"), new Vector2Int(4, 5), BuildingCategory.ACCOMMODATION, 1000),
    BAND2("Band 2 Accommodation", new Texture("buildings/Band2.png"), new Vector2Int(4, 5), BuildingCategory.ACCOMMODATION, 1000),
    BAND3("Band 3 Accommodation", new Texture("buildings/Band3.png"), new Vector2Int(4, 5), BuildingCategory.ACCOMMODATION, 1000),
    BAND4("Band 4 Accommodation", new Texture("buildings/Band4.png"), new Vector2Int(4, 5), BuildingCategory.ACCOMMODATION, 1000),
    // Recreational
    OUTDOORGYM("Outdoor Gym", new Texture("buildings/Outdoor Gym.png"), new Vector2Int(4, 4), BuildingCategory.RECREATIONAL, 1000),
    PARK("Park", new Texture("buildings/Park.png"), new Vector2Int(8, 4), BuildingCategory.RECREATIONAL, 1000),
    STATIONERYSTORE("Stationery Store", new Texture("buildings/Stationery Store.png"), new Vector2Int(4, 4), BuildingCategory.RECREATIONAL, 1000),
    //Food
    CAFETERIA("Cafeteria", new Texture("buildings/Cafeteria.png"), new Vector2Int(6, 6), BuildingCategory.FOOD, 1000),
    FOODSTORE("Food Store", new Texture("buildings/Food Store.png"), new Vector2Int(4, 4), BuildingCategory.FOOD, 1000),
    //Educational
    OFFICE("Office", new Texture("buildings/Office.png"), new Vector2Int(6, 6), BuildingCategory.EDUCATIONAL, 1000),
    LIBRARY("Library", new Texture("buildings/Library.png"), new Vector2Int(4, 5), BuildingCategory.EDUCATIONAL, 1000),
    ;

    private final String displayName;
    private final Texture texture;
    private final Vector2Int size;
    private final BuildingCategory category;
    private final float cost;

    /**
     * Create a {@link BuildingType} with specified attributes.
     *
     * @param displayName the name to display when selecting this building.
     * @param texture     the texture representing the building.
     * @param size        the size of the building (in tiles).
     * @param category    the category of the building.
     * @param cost        the cost to place the building.
     */
    BuildingType(String displayName, Texture texture, Vector2Int size, BuildingCategory category, float cost) {
        this.displayName = displayName;
        this.texture = texture;
        this.size = size;
        this.category = category;
        this.cost = cost;
    }


    public Texture getTexture() {
        return texture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Vector2Int getSize() {
        return size;
    }

    public BuildingCategory getCategory() {
        return category;
    }

    public float getCost() {
        return cost;
    }

    public static BuildingType[] getBuildingsOfType(BuildingCategory category) {
        return Stream.of(BuildingType.values())
            .filter(buildingType -> buildingType.getCategory().equals(category))
            .toArray(BuildingType[]::new);

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
