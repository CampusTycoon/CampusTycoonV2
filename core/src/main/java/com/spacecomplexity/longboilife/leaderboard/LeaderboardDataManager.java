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
            // Load existing entries
            List<LeaderboardEntry> existingEntries = loadLeaderboard();
            
            // Add all new entries
            existingEntries.addAll(entries);
            
            // Sort the combined list in descending order by score
            existingEntries.sort((e1, e2) -> Float.compare(e2.getScore(), e1.getScore()));
            
            // Save the sorted list
            FileHandle file = Gdx.files.local(LEADERBOARD_FILE);
            String jsonString = json.prettyPrint(existingEntries);
            file.writeString(jsonString, false);  // Use false to overwrite
        } catch (Exception e) {
            Gdx.app.error("LeaderboardDataManager", "Error saving leaderboard", e);
        }
    }
} 