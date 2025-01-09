package com.spacecomplexity.longboilife.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Abstract class to represent a UI element block.
 */
public abstract class UIElement {
    protected Viewport uiViewport;
    protected Table table;
    protected Table parentTable;
    protected final Skin skin;

    /**
     * Initialise base UI elements
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIElement(Viewport uiViewport, Table parentTable, Skin skin) {
        this.uiViewport = uiViewport;
        this.parentTable = parentTable;
        this.skin = skin;

        // Initialise table container
        table = new Table(skin);

        // Add table to root table
        parentTable.addActor(table);
    }

    /**
     * Render this UI element.
     */
    public abstract void render();

    /**
     * Handles resizing events, to ensure the placement of element is mapped correctly.
     */
    public void resize() {
        placeTable();
    }

    /**
     * Set the table position relative to the viewport.
     */
    abstract protected void placeTable();

    /**
     * Dispose of all loaded assets.
     */
    public void dispose() {
    }
}
