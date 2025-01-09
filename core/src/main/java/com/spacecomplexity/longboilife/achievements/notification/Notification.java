package com.spacecomplexity.longboilife.achievements.notification;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.ui.UIElement;

public class Notification extends UIElement {
    private static final float DISPLAY_DURATION = 3f; // Show for 3 seconds
    private static final float FADE_DURATION = 0.5f;  // Fade in/out over 0.5 seconds

    public Notification(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);
        
        table.setBackground(skin.getDrawable("panel1"));
        table.setVisible(false);
        table.setSize(315, 100);
        placeTable();
    }

    public void showAchievementUnlock(String title, String description) {
        table.clear();

        // Create labels for achievement info with smaller font scale
        Label titleLabel = new Label("Achievement Unlocked!", this.skin, "title");
        titleLabel.setFontScale(1f);  // Make the title smaller
        
        Label achievementTitle = new Label(title, this.skin);
        achievementTitle.setFontScale(1f);  // Make achievement name smaller
        
        Label descriptionLabel = new Label(" - " + description, this.skin);
        descriptionLabel.setFontScale(1f);  // Make description smaller

        // Create a table for the title and description to be on same line
        Table infoTable = new Table();
        infoTable.add(achievementTitle);
        infoTable.add(descriptionLabel);

        // Add labels to table with title above, reduce padding
        table.add(titleLabel).padLeft(0).padTop(3).row();
        table.add(infoTable).padLeft(0).padBottom(3);

        // Position the notification
        placeTable();

        // Show notification with animation
        table.setVisible(true);
        table.getColor().a = 0f; // Start fully transparent
        
        // Create fade in -> wait -> fade out sequence
        table.addAction(Actions.sequence(
            Actions.fadeIn(FADE_DURATION),
            Actions.delay(DISPLAY_DURATION),
            Actions.fadeOut(FADE_DURATION),
            Actions.run(() -> table.setVisible(false))
        ));
    }

    @Override
    protected void placeTable() {
        // Position in bottom-left corner, above the UI bar
        table.setPosition(
            10,  // Left padding of 10px
            60 + 10  // Bottom menu height (60) + padding (10)
        );
    }

    @Override
    public void render() {
        // No per-frame updates needed since notifications are handled by actions
    }
}
