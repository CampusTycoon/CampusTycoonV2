package com.spacecomplexity.longboilife.leaderboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardDataManager {
    private static final String LEADERBOARD_FILE = "leaderboard.json";
    private final Json json;
    
    public LeaderboardDataManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
    }
    
    public List<LeaderboardEntry> loadLeaderboard() {
        try {
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return json.fromJson(ArrayList.class, LeaderboardEntry.class, file);
        } catch (Exception e) {
            Gdx.app.error("LeaderboardDataManager", "Error loading leaderboard", e);
            return new ArrayList<>();
        }
    }
    
    public void saveLeaderboard(List<LeaderboardEntry> entries) {
        try {
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            String jsonString = json.toJson(entries);
            file.writeString(jsonString, false);
        } catch (Exception e) {
            Gdx.app.error("LeaderboardDataManager", "Error saving leaderboard", e);
        }
    }
} 