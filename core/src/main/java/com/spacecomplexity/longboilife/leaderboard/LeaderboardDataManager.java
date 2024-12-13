package com.spacecomplexity.longboilife.leaderboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class LeaderboardDataManager {
    private static final String LEADERBOARD_FILE = "leaderboard.json";
    private final Json json;
    
    public LeaderboardDataManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
    }
    
    public List<LeaderboardEntry> loadLeaderboard() {
        try {
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            if (!file.exists() || file.length() == 0) { //If file doesn't exist or is empty, return an empty ArrayList
                return new ArrayList<>();
            }
            String content = file.readString().trim();
            if (content.isEmpty()) {
                return new ArrayList<>();
            }
            LeaderboardEntry[] entries = json.fromJson(LeaderboardEntry[].class, file);
            return new ArrayList<>(Arrays.asList(entries));
        } catch (Exception e) { //Error message
            Gdx.app.error("LeaderboardDataManager", "Error loading leaderboard", e);
            return new ArrayList<>();
        }
    }
    
    public void saveLeaderboard(List<LeaderboardEntry> entries) {
        try {
            // TODO: Sort the entries by score before saving so scores don't have to be sorted every time leaderboard is displayed
            // Load existing entries first
            List<LeaderboardEntry> existingEntries = loadLeaderboard();
            // Add all new entries ensures the structure of the JSON is kept
            // This is important, as not keeping the structurs causes errors when trying to read the file
            existingEntries.addAll(entries);
            
            // Save the combined list (overwriting the file)
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            String jsonString = json.prettyPrint(existingEntries);
            file.writeString(jsonString, false);  // Use false to overwrite
        } catch (Exception e) {
            Gdx.app.error("LeaderboardDataManager", "Error saving leaderboard", e);
        }
    }
} 