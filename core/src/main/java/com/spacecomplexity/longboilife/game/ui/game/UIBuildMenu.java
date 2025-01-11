package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.ui.UIElement;
import com.spacecomplexity.longboilife.game.utils.EventHandler;
import com.spacecomplexity.longboilife.game.utils.Events;
import com.spacecomplexity.longboilife.game.utils.Vector2Int;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class to represent the Build Menu UI.
 */
public class UIBuildMenu extends UIElement {
    private final Skin skin;

    /**
     * Initialise build menu elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIBuildMenu(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        this.skin = skin;

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        placeTable();

        // Initially close the menu
        closeMenu();

        // Close menu when receiving an event to do so
        EventHandler.getEventHandler().createEvent(Events.Event.CLOSE_BUILD_MENU, (params) -> {
            closeMenu();
            return null;
        });
    }

    public void render() {
    }

    /**
     * Open the build menu ready for the user to place buildings from a specific {@link BuildingCategory}.
     *
     * @param category specific category of buildings to show.
     */
    public void openMenu(BuildingCategory category) {
        // Clear previous buildings from the table
        table.clear();

        // Get list of all buildings to display on this menu
        BuildingType[] buildings = BuildingType.getBuildingsOfType(category);

        // Place building buttons on separate table for condensed styling
        Table buildingButtonsTable = new Table();
        for (BuildingType building : buildings) {
            // Get building texture
            TextureRegionDrawable texture = new TextureRegionDrawable(building.getTexture());
            // Set the size of the texture keeping its aspect ratio
            float textureSize = table.getHeight() - 75;
            Vector2Int buildingSize = building.getSize();
            float maxDimension = Math.max(buildingSize.x, buildingSize.y);
            texture.setMinSize(textureSize * (buildingSize.x / maxDimension), textureSize * (buildingSize.y / maxDimension));

            // Initialise building button
            ImageButton button = new ImageButton(texture);
            // Initialise place building sequence when clicked
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    EventHandler.getEventHandler().callEvent(Events.Event.CANCEL_OPERATIONS);

                    GameState.getState().placingBuilding = building;
                }
            });

            // Initialise building labels
            Label titleLabel = new Label(building.getDisplayName(), skin);
            Label costLabel = new Label(NumberFormat.getCurrencyInstance(Locale.UK).format(building.getCost()), skin);
            Label sizeLabel = new Label(String.format("%dx%d", building.getSize().x, building.getSize().y), skin);

            // create container for UI elements relating to this building
            Table buildingTable = new Table();
            // Add building UI elements into their container
            buildingTable.add(button);
            buildingTable.row();
            buildingTable.add(titleLabel).padTop(2);
            buildingTable.row();
            buildingTable.add(costLabel).padTop(2);
            buildingTable.row();
            buildingTable.add(sizeLabel).padTop(2);

            // Add the container to the building table
            buildingButtonsTable.add(buildingTable).expandX().expandY().fillY().padLeft(2);
        }
        // Add the buildings table onto the main table
        table.add(buildingButtonsTable).expandX().left();

        // Show the build menu
        table.setVisible(true);

        // Update game state
        GameState.getState().buildMenuOpen = true;
    }

    /**
     * Close the build menu.
     */
    public void closeMenu() {
        // Hide the build menu
        table.setVisible(false);

        // Update game state
        GameState.getState().buildMenuOpen = false;
    }

    @Override
    protected void placeTable() {
        table.setSize(uiViewport.getWorldWidth(), 150);
        table.setPosition(0, 55);
    }
}
