package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.ui.UIElement;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Class to represent the UI building counter.
 */
public class UIBuildingCounter extends UIElement {
    private Label counterLabel;
    private static final float height = 110f;

    /**
     * Initialise clock menu elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIBuildingCounter(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        // Initialise building label - filter out PATHWAY category
        String buildingList = String.join(
            "\r\n",
            Arrays.stream(BuildingCategory.values())
                .filter(category -> category != BuildingCategory.PATHWAY)
                .map(BuildingCategory::getDisplayName)
                .toArray(String[]::new)
        );
        Label buildingLabel = new Label(buildingList, skin);
        buildingLabel.setFontScale(1f);
        buildingLabel.setColor(Color.WHITE);

        // Initialise counter label
        counterLabel = new Label(null, skin);
        counterLabel.setFontScale(1f);
        counterLabel.setColor(Color.WHITE);

        // Place labels onto table
        table.left().padLeft(15).add(buildingLabel);
        table.add(counterLabel).padLeft(5);

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        table.setSize(150, height);
        placeTable();
    }

    public void render() {
        // Get the count of buildings by category
        String buildingCount = String.join(
            "\r\n",
            Arrays.stream(BuildingCategory.values())
                .filter(category -> category != BuildingCategory.PATHWAY)
                .map(category -> String.valueOf(getCategoryCount(category)))
                .toArray(String[]::new)
        );
        counterLabel.setText(buildingCount);
    }

    /**
     * Helper function to get the total count of buildings in a category.
     *
     * @param category the building category to count.
     * @return the total number of buildings in this category.
     */
    private int getCategoryCount(BuildingCategory category) {
        return Stream.of(BuildingType.values())
            .filter(buildingType -> buildingType.getCategory() == category)
            .mapToInt(buildingType -> GameState.getState().getBuildingCount(buildingType))
            .sum();
    }

    @Override
    protected void placeTable() {
        table.setPosition(0, uiViewport.getWorldHeight() - table.getHeight());
    }

    public static float getHeight() {
        return height;
    }
}
