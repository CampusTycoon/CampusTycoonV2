package com.spacecomplexity.longboilife.game.ui.gameover;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.ui.UIElement;
import com.spacecomplexity.longboilife.game.utils.EventHandler;
import java.util.List;
import java.util.ArrayList;

import com.spacecomplexity.longboilife.leaderboard.LeaderboardDataManager;
import com.spacecomplexity.longboilife.leaderboard.LeaderboardEntry;
/**
 * Class to represent the Overview UI after the game is completed.
 */
public class UIOverview extends UIElement {

    private LeaderboardDataManager dataManager;
    private List<LeaderboardEntry> entries;

    /**
     * Initialise overview elements.
     *
     * @param uiViewport  the viewport used to render UI.
     * @param parentTable the table to render this element onto.
     * @param skin        the provided skin.
     */
    public UIOverview(Viewport uiViewport, Table parentTable, Skin skin) {
        super(uiViewport, parentTable, skin);

        // Initialize the data manager
        this.dataManager = new LeaderboardDataManager();

        String overview = String.format("Game Over\r\nSatisfaction Score: %.2f%%", GameState.getState().satisfactionScore * 100);

        // Initialise label
        Label label = new Label(overview, skin);
        label.setAlignment(Align.center);
        label.setFontScale(1.2f);
        label.setColor(Color.WHITE);

        // Add username text field
        Label usernameLabel = new Label("Enter username:", skin);
        TextField usernameField = new TextField("", skin);
        usernameField.setMaxLength(20); // Limit username length
        usernameField.setMessageText("Enter your username");

        // Initialise button
        TextButton button = new TextButton("Menu", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Getting username and score which will be saved to Leaderboard JSON file
                String username = usernameField.getText();
                Float userScore = GameState.getState().satisfactionScore;

                // Adding user's username and score to the leaderboard JSON file
                List<LeaderboardEntry> newEntries = new ArrayList<>();
                newEntries.add(new LeaderboardEntry(username, userScore));
                dataManager.saveLeaderboard(newEntries);
                
                // Return to menu once button is clicked
                EventHandler.getEventHandler().callEvent(EventHandler.Event.RETURN_MENU);
            }
        });

        // Place elements onto table
        table.add(label).align(Align.center);
        table.row();
        table.add(usernameLabel).padTop(10).align(Align.center);
        table.row();
        table.add(usernameField).width(200).padTop(5).align(Align.center);
        table.row();
        table.add(button).padTop(10).align(Align.center);

        // Style and place the table
        table.setBackground(skin.getDrawable("panel1"));
        table.setSize(220, 160); // Increased height to accommodate new elements
        placeTable();
    }

    public void render() {
    }

    @Override
    protected void placeTable() {
        table.setPosition((uiViewport.getWorldWidth() - table.getWidth()) / 2, uiViewport.getWorldHeight() - table.getHeight());
    }
}
