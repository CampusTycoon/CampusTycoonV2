package com.spacecomplexity.longboilife.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.achievements.notification.Notification;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AchievementManager {
    private static final String ACHIEVEMENTS_FILE = "achievements.json";
    private static AchievementManager instance;
    private final List<Achievement> achievements;
    private final Json json;
    private Notification notification;
    
    private AchievementManager() {
        achievements = new ArrayList<>();
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        
        // Initialize default achievements
        initializeAchievements();
        loadAchievements();
    }
    
    public void initializeNotification(Viewport viewport, Table parentTable, Skin skin) {
        notification = new Notification(viewport, parentTable, skin);
    }
    
    private void initializeAchievements() {
        achievements.add(new Achievement(
            "highly_satisfied",
            "Highly Satisfied",
            "Satisfaction score of 90% or more",
            Achievement.AchievementType.SATISFACTION_SCORE
        ));
        
        achievements.add(new Achievement(
            "campus_master",
            "Campus Master",
            "Build 30 buildings",
            Achievement.AchievementType.BUILDING_COUNT
        ));

        achievements.add(new Achievement(
            "budget_master",
            "Budget Master",
            "Never go below £400,000",
            Achievement.AchievementType.BUDGET
        ));
    }
    
    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }
    
    public void checkAchievements() {
        for (Achievement achievement : achievements) {
            if (!achievement.isUnlocked()) {
                switch (achievement.getType()) {
                    case SATISFACTION_SCORE:
                        checkSatisfactionAchievements(achievement);
                        break;
                    case BUILDING_COUNT:
                        checkBuildingAchievements(achievement);
                        break;
                    case BUDGET:
                        checkBudgetAchievements(achievement);
                        break;
                    // Add more cases for different achievement types
                }
            }
        }
    }
    
    private void checkSatisfactionAchievements(Achievement achievement) {
        double satisfactionScore = GameState.getState().satisfactionScore;
        if (achievement.getId().equals("highly_satisfied") && satisfactionScore >= 90.0) {
            unlockAchievement(achievement);
        }
    }
    
    private void checkBuildingAchievements(Achievement achievement) {
        int totalBuildings = 0;
        for (BuildingType type : BuildingType.values()) {
            // Skip counting roads/pathways6
            if (type.getCategory() != BuildingCategory.PATHWAY) {
                totalBuildings += GameState.getState().getBuildingCount(type);
            }
        }
        
        if (achievement.getId().equals("campus_master") && totalBuildings >= 30) {
            unlockAchievement(achievement);
        }
    }
    
    private void checkBudgetAchievements(Achievement achievement) {
        double currentBudget = GameState.getState().getBudget();
        if (achievement.getId().equals("budget_master")) {
            if (currentBudget < 400000) {
                // If budget drops below 400,000, mark the achievement as failed
                achievement.setFailed(true);
            } else if (!achievement.isFailed() && GameState.getState().gameOver) {
                // Only award the achievement if the game is over and we never dropped below 400,000
                unlockAchievement(achievement);
            }
        }
    }
    
    private void unlockAchievement(Achievement achievement) {
        achievement.setUnlocked(true);
        saveAchievements();
        if (notification != null) {
            notification.showAchievementUnlock(achievement.getTitle(), achievement.getDescription());
        }
        Gdx.app.log("Achievement Unlocked", achievement.getTitle() + " - " + achievement.getDescription()); //Troubleshooting
    }
    
    private void saveAchievements() {
        try {
            FileHandle file = Gdx.files.local(ACHIEVEMENTS_FILE);
            String jsonString = json.prettyPrint(achievements);
            file.writeString(jsonString, false);
        } catch (Exception e) {
            Gdx.app.error("AchievementManager", "Error saving achievements", e);
        }
    }
    
    private void loadAchievements() {
        try {
            FileHandle file = Gdx.files.local(ACHIEVEMENTS_FILE);
            if (!file.exists() || file.length() == 0) { //If file doesn't exist or is empty, return an empty ArrayList
                return;
            }
            String content = file.readString().trim();
            if (content.isEmpty()) { //If file is empty, return an empty ArrayList
                return;
            }
            Achievement[] loadedAchievements = json.fromJson(Achievement[].class, file);
            achievements.clear();
            achievements.addAll(Arrays.asList(loadedAchievements));
        } catch (Exception e) {
            Gdx.app.error("AchievementManager", "Error loading achievements", e);
        }
    }
}
