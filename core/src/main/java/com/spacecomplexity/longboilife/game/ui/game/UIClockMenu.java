package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.globals.MainTimer;
import com.spacecomplexity.longboilife.game.ui.UIElement;

/**
 * Class to represent the Clock UI.
 */
public class UIClockMenu extends UIElement {
    private Label label;

    private final MainTimer mainTimer = MainTimer.getTimerManager();

    /**
     * Initialise clock menu elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIClockMenu(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        // Initialise time label
        label = new Label(null, skin);
        label.setFontScale(1.5f);
        label.setColor(Color.WHITE);

        // Place label onto table
        table.add(label).align(Align.center);

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        table.setSize(75, 50);
        placeTable();
    }

    public void render() {
        setTime(mainTimer.getTimer().getTimeLeft() / 1000);
    }

    /**
     * Set the labels on the clock menu to the time given.
     *
     * @param time the time in seconds
     */
    private void setTime(long time) {
        // Format this onto the time label
        label.setText(String.format("%d:%02d", time / 60, time % 60));
    }

    @Override
    protected void placeTable() {
        table.setPosition(0, uiViewport.getWorldHeight() - UIBuildingCounter.getHeight() - table.getHeight() + 5);
    }
}
