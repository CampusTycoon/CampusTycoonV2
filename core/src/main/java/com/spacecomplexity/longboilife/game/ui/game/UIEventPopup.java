package com.spacecomplexity.longboilife.game.ui.game;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.ui.UIElement;
import com.spacecomplexity.longboilife.game.utils.EventHandler;
import com.spacecomplexity.longboilife.game.utils.Events;

public class UIEventPopup extends UIElement {
    private static final float DISPLAY_DURATION = 5f; // Show for 5 seconds
    private static final float FADE_DURATION = 0.5f;  // Fade in/out over 0.5 seconds
    private Label messageLabel;

    public UIEventPopup(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        table.setVisible(false);
        
        // Create message label with word wrap
        messageLabel = new Label("", skin);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.center);
        
        // Add message label to table
        table.add(messageLabel).width(400f).pad(20f);
        
        placeTable();

        // Close menu when receiving an event to do so
        EventHandler.getEventHandler().createEvent(Events.Event.CLOSE_EVENT_POPUP, (params) -> {
            close();
            return null;
        });
    }
    
    public void showEvent(String message) {
        // Set the message
        messageLabel.setText(message);
        
        // Pack the table to fit the content
        table.pack();
        
        // Position the popup
        placeTable();
        
        // Show popup with animation
        table.setVisible(true);
        table.getColor().a = 0f; // Start fully transparent
        
        // Create fade in -> wait -> fade out sequence
        table.clearActions();
        table.addAction(Actions.sequence(
            Actions.fadeIn(FADE_DURATION),
            Actions.delay(DISPLAY_DURATION),
            Actions.fadeOut(FADE_DURATION),
            Actions.run(() -> close())
        ));
    }
    
    private void close() {
        table.setVisible(false);
        table.clearActions();
    }

    @Override
    protected void placeTable() {
        // Center the popup on screen
        float x = (uiViewport.getWorldWidth() - table.getWidth()) / 2;
        float y = (uiViewport.getWorldHeight() - table.getHeight()) / 2;
        table.setPosition(x, y);
    }
    
    @Override
    public void render() {
        // No per-frame updates needed since animations are handled by actions
    }
}
