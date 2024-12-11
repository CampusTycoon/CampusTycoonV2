package com.spacecomplexity.longboilife.leaderboard;

public class LeaderboardEntry {
    private String username;
    private int score;
    
    // Required for JSON serialization
    public LeaderboardEntry() {}
    public LeaderboardEntry(String username, int score) {
        this.username = username;
        this.score = score;
    }
    
    // Getters and setters
    public String getUsername() { 
        return username; 
    }

    public void setUsername(String username) { 
        this.username = username; 
    }

    public int getScore() { 
        return score; 
    }
    
    public void setScore(int score) { 
        this.score = score; 
    }
}