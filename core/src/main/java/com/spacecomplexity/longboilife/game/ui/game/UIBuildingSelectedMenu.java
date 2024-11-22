package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainCamera;
import com.spacecomplexity.longboilife.game.ui.UIElement;
import com.spacecomplexity.longboilife.game.utils.EventHandler;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class to represent the pop-up menu after selecting a placed building.
 */
public class UIBuildingSelectedMenu extends UIElement {
    private TextButton moveButton;
    private TextButton sellButton;

    private Vector3 worldSpaceOpened;

    /**
     * Initialise selected menu elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIBuildingSelectedMenu(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        this.uiViewport = uiViewport;

        EventHandler eventHandler = EventHandler.getEventHandler();

        // Initialise move button
        moveButton = new TextButton("Move", skin);
        moveButton.pad(10);
        // move the selected building when clicked
        moveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Call the events to sell the selected building
                eventHandler.callEvent(EventHandler.Event.MOVE_BUILDING);
            }
        });

        // Initialise sell button
        sellButton = new TextButton("Sell", skin);
        sellButton.pad(10);
        // Sell the selected building when clicked
        sellButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Call the events to sell the selected building
                eventHandler.callEvent(EventHandler.Event.SELL_BUILDING);

                closeMenu();
            }
        });

        // Place buttons onto table
        table.add(moveButton);
        table.add(sellButton).padLeft(2);

        // Style and place the table
        table.setSize(175, 75);
        table.setBackground(skin.getDrawable("panel1"));
        placeTable();

        // Initially close the menu
        closeMenu();

        // Open menu when receiving an event to do so
        eventHandler.createEvent(EventHandler.Event.OPEN_SELECTED_MENU, (params) -> {
            openMenu();
            return null;
        });

        // Close menu when receiving an event to do so
        eventHandler.createEvent(EventHandler.Event.CLOSE_SELECTED_MENU, (params) -> {
            closeMenu();
            return null;
        });
    }

    public void render() {
        // Keep the menu placed relative to world space
        if (worldSpaceOpened != null) {
            Vector3 placement = new Vector3(worldSpaceOpened);
            // Convert world space into screen space
            MainCamera.camera().getCamera().project(placement);
            // Flip y coordinates as screen/ui coordinate system has a flipped origin
            placement.y = Gdx.graphics.getHeight() - placement.y;
            // Convert screen space into ui coordinates
            uiViewport.unproject(placement);
            // Set the menu position at this location
            table.setPosition(placement.x, placement.y);
        }
    }

    private void openMenu() {
        // Get the world space of current mouse position so that we can keep it positioned relatively
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        MainCamera.camera().getCamera().unproject(mouse);
        worldSpaceOpened = new Vector3(mouse);

        // Set the prices of each of these actions based on the price of the building and constants
        float buildingCost = GameState.getState().selectedBuilding.getType().getCost();
        moveButton.setText("Move\r\n" + NumberFormat.getCurrencyInstance(Locale.UK).format(buildingCost * Constants.moveCostRecovery));
        moveButton.pack();
        sellButton.setText("Sell\r\n+" + NumberFormat.getCurrencyInstance(Locale.UK).format(buildingCost * Constants.sellCostRecovery));
        sellButton.pack();

        // Set the table size to match the size of the buttons
        table.setSize(moveButton.getWidth() + sellButton.getWidth() + 25, table.getHeight());

        table.setVisible(true);
    }

    private void closeMenu() {
        // Remove the open coordinates so we don't calculate the menus position every frame
        worldSpaceOpened = null;

        table.setVisible(false);
    }

    @Override
    protected void placeTable() {
        // Element can move a lot so is placed in the render loop
    }
}
