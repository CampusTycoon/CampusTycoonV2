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
import com.spacecomplexity.longboilife.game.ui.UIManager;

public class AchievementManager {
    private static final String ACHIEVEMENTS_FILE = "achievements.json";
    private static AchievementManager instance;
    private final List<Achievement> achievements;
    private final Json json;
    
    private AchievementManager() {
        achievements = new ArrayList<>();
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        
        // Initialize default achievements
        initializeAchievements();
        loadAchievements();
    }
    
    private void initializeAchievements() {
        achievements.add(new Achievement(
            "highly_satisfied",
            "Highly Satisfied",
            "Reach a satisfaction score of 90% or higher",
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
            "Never go below £x",
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
                }
            }
        }
    }
    
    private void checkSatisfactionAchievements(Achievement achievement) {
        double satisfactionScore = GameState.getState().satisfactionScore;
        if (achievement.getId().equals("satisfaction_master") && satisfactionScore >= 1.0) { //TODO: Actually implement this with satisfaction score once that's added
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
        // TODO: Implement budget achievement logic
    }
    
    private void unlockAchievement(Achievement achievement) {
        achievement.setUnlocked(true);
        saveAchievements();
        
        // Show notification
        if (UIManager.getInstance() != null) {
            UIManager.getInstance().showAchievementNotification(
                achievement.getTitle(),
                achievement.getDescription()
            );
        }
        
        Gdx.app.log("Achievement Unlocked", achievement.getTitle() + " - " + achievement.getDescription());
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
            if (!file.exists() || file.length() == 0) {
                return;
            }
            String content = file.readString().trim();
            if (content.isEmpty()) {
                return;
            }
            Achievement[] loadedAchievements = json.fromJson(Achievement[].class, content);
            achievements.clear();
            achievements.addAll(Arrays.asList(loadedAchievements));
        } catch (Exception e) {
            Gdx.app.error("AchievementManager", "Error loading achievements", e);
        }
    }
}
