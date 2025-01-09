package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.ui.UIElement;

/**
 * Class to represent the Satisfaction Score UI.
 */
public class UISatisfactionMenu extends UIElement {
    private Label scoreLabel;
    private ProgressBar satisfactionBar;

    /**
     * Initialise satisfaction menu elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UISatisfactionMenu(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        // Initialise label
        Label label = new Label("Satisfaction Score:", skin);
        label.setFontScale(1f);
        label.setColor(Color.WHITE);

        // Initialise score label
        scoreLabel = new Label(null, skin);
        scoreLabel.setFontScale(1f);

        // Initialise satisfaction bar
        satisfactionBar = new ProgressBar(0, 1, 0.01f, false, skin);

        // Place elements onto table
        Table labelTable = new Table(skin);
        labelTable.add(label);
        labelTable.add(scoreLabel).padLeft(5);
        table.add(labelTable);
        table.row();
        table.add(satisfactionBar).padTop(2);

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        table.setSize(185, 60);
        placeTable();
    }

    public void render() {
        double satisfaction = GameState.getState().satisfactionScore;
        
        scoreLabel.setText(String.format("%.2f%%", satisfaction));
        scoreLabel.setColor(satisfaction > 50 ? Color.GREEN : Color.RED);
        satisfactionBar.setValue((float)satisfaction / 100);
    }

    @Override
    protected void placeTable() {
        table.setPosition(uiViewport.getWorldWidth() - table.getWidth(), uiViewport.getWorldHeight() - table.getHeight());
    }
}
