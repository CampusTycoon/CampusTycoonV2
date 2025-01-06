package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.ui.UIElement;

public class UITooltip extends UIElement {
    private static final float HOVER_DELAY = 0.5f; // 0.5 seconds
    private final GameState gameState = GameState.getState();
    private final Skin skin;

    public UITooltip(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);
        this.skin = skin;
        
        table.setBackground(skin.getDrawable("panel1"));
        table.setVisible(false);
    }

    public void render() {
        if (gameState.hoveredBuilding != null) {
            gameState.buildingHoverTime += Gdx.graphics.getDeltaTime();
            
            if (gameState.buildingHoverTime >= HOVER_DELAY && !table.isVisible()) {
                showTooltip(gameState.hoveredBuilding);
            }
        } else {
            hideTooltip();
            gameState.buildingHoverTime = 0f;
        }
        
        // Update position to follow mouse
        if (table.isVisible()) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY() - 30, 0);
            uiViewport.unproject(mousePos); // Convert screen coordinates to viewport coordinates
            table.setPosition(mousePos.x, mousePos.y);
        }
    }

    private void showTooltip(Building building) {
        table.clear();
        
        // Add building information
        table.add(new Label(building.getType().getDisplayName(), skin, "title")).row();
        table.add(new Label("Type: " + building.getType().getCategory().getDisplayName(), skin)).row();
        table.add(new Label("Size: " + building.getType().getSize().x + "x" + building.getType().getSize().y, skin)).row();
        
        table.pack(); // Resize to fit content
        table.setVisible(true);
    }

    private void hideTooltip() {
        table.setVisible(false);
    }

    @Override
    protected void placeTable() {
        // Position is updated in render()
    }
} 