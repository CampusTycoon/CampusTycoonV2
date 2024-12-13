package com.spacecomplexity.longboilife.leaderboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.spacecomplexity.longboilife.Main;
import com.spacecomplexity.longboilife.MainInputManager;
import com.spacecomplexity.longboilife.game.globals.Window;
import java.util.List;
import java.util.ArrayList;

/**
 * Main class to control the menu screen.
 */
public class LeaderboardScreen implements Screen {
    private final Main game;
    private final Main.ScreenType previousScreen;

    private Viewport viewport;

    private Texture backgroundTexture;
    private SpriteBatch batch;

    private Stage stage;
    private Skin skin;

    private LeaderboardDataManager dataManager;
    private List<LeaderboardEntry> entries;

    public LeaderboardScreen(Main game, Main.ScreenType previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        // Initialise viewport and drawing elements
        viewport = new ScalingViewport(Scaling.fit, Window.DEFAULT_WIDTH, Window.DEFAULT_HEIGHT);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal("menu/Plain Black.png"));

        // Load UI skin for buttons
        skin = new Skin(Gdx.files.internal("ui/skin/uiskin.json"));

        dataManager = new LeaderboardDataManager();
        entries = dataManager.loadLeaderboard();

        // Testing whether adding scores to the leaderboard works
        //List<LeaderboardEntry> newEntries = new ArrayList<>();
        //newEntries.add(new LeaderboardEntry("Test5", 556300));
        //dataManager.saveLeaderboard(newEntries);

        for (LeaderboardEntry entry : entries) {
            System.out.println(entry.getUsername() + ": " + entry.getScore());
        }
    }

    @Override
    public void show() {
        // Clear any existing actors before setting up new ones
        stage.clear();

        // Table layout for menu alignment
        Table table = new Table();
        table.setFillParent(true);
        table.top(); // Align table contents to the top
        stage.addActor(table);

        // Create a table to display one row of labels
        // TODO: These are currently buttons, but should be labels
        Table labelsRow = new Table();
        labelsRow.setSkin(skin);
        labelsRow.add(new TextButton("Username", skin, "round")).left();
        labelsRow.add().expandX();
        labelsRow.add(new TextButton("Score", skin, "round")).right();

        // Add labels to table
        table.add(labelsRow).expandX().fillX().pad(100);
        table.row();
        
        // Add entries to the table
        // TODO: Pull all data from leaderboard file, only display top X amount of scores/usernames
        for (LeaderboardEntry entry : entries) {
            Table row = new Table();
            row.add(new Label(entry.getUsername(), skin)).left();
            row.add().expandX();
            row.add(new Label(String.valueOf(entry.getScore()), skin)).right();
            table.add(row).expandX().fillX().pad(10);
            table.row();
        }
        
        // Initialise back button
        TextButton backButton = new TextButton("Back", skin, "round");
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.switchScreen(previousScreen);
            }
        });

        // Add buttons to table with bottom alignment
        table.row();  // Move to next row
        table.add(backButton).colspan(2).padBottom(10);  // colspan(2) makes button span both columns

        // Allows UI to capture touch events
        InputMultiplexer inputMultiplexer = new InputMultiplexer(new MainInputManager(), stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1f);

        // Draw background image
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Window.DEFAULT_HEIGHT, Window.DEFAULT_HEIGHT);
        batch.end();

        // Draw and apply ui
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        Window.update(width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
        batch.dispose();
    }
}
