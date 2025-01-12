package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.ui.UIElement;
import com.spacecomplexity.longboilife.game.utils.EventHandler;
import com.spacecomplexity.longboilife.game.utils.Events;

public class UIEventPopup extends UIElement {

    public UIEventPopup(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        placeTable();

        // Close menu when receiving an event to do so
        EventHandler.getEventHandler().createEvent(Events.Event.CLOSE_EVENT_POPUP, (params) -> {
            close();
            return null;
        });
    }
    
    private void close() {
        // Close the popup
    }
    @Override
    protected void placeTable() {
        
    }
    
    @Override
    public void render() {
    }
}
