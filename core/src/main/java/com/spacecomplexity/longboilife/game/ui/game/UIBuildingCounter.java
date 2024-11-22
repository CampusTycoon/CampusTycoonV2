package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.ui.UIElement;

import java.util.Arrays;

/**
 * Class to represent the UI building counter.
 */
public class UIBuildingCounter extends UIElement {
    private Label counterLabel;

    /**
     * Initialise clock menu elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIBuildingCounter(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        // Initialise building label
        String buildingList = String.join(
            "\r\n",
            Arrays.stream(BuildingType.values())
                .map(BuildingType::getDisplayName)
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
        table.setSize(100, 110);
        placeTable();
    }

    public void render() {
        // Get the count of all buildings and display them
        String buildingCount = String.join(
            "\r\n",
            Arrays.stream(BuildingType.values())
                .map((buildingType -> GameState.getState().getBuildingCount(buildingType).toString()))
                .toArray(String[]::new)
        );
        counterLabel.setText(buildingCount);
    }

    @Override
    protected void placeTable() {
        table.setPosition(0, uiViewport.getWorldHeight() - table.getHeight() - 45);
    }
}
