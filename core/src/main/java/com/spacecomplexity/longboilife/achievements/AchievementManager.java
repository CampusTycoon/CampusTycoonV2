package com.spacecomplexity.longboilife.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.ArrayList;
import java.util.List;
import com.spacecomplexity.longboilife.achievements.Achievement;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.building.BuildingType;

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
                    // Add more cases for different achievement types
                }
            }
        }
    }
    
    private void checkSatisfactionAchievements(Achievement achievement) {
        float satisfactionScore = GameState.getState().satisfactionScore;
        if (achievement.getId().equals("satisfaction_master") && satisfactionScore >= 1.0f) {
            unlockAchievement(achievement);
        }
    }
    
    private void checkBuildingAchievements(Achievement achievement) {
        int totalBuildings = 0;
        for (BuildingType type : BuildingType.values()) {
            totalBuildings += GameState.getState().getBuildingCount(type);
        }
        
        if (achievement.getId().equals("building_tycoon") && totalBuildings >= 20) {
            unlockAchievement(achievement);
        }
    }
    
    private void unlockAchievement(Achievement achievement) {
        achievement.setUnlocked(true);
        saveAchievements();
        // TODO: Show achievement notification
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
            if (file.exists()) {
                List<Achievement> savedAchievements = json.fromJson(ArrayList.class, Achievement.class, file);
                achievements.clear();
                achievements.addAll(savedAchievements);
            }
        } catch (Exception e) {
            Gdx.app.error("AchievementManager", "Error loading achievements", e);
        }
    }
}
