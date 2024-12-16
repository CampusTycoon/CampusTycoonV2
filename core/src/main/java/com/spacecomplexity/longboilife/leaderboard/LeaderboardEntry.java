package com.spacecomplexity.longboilife.leaderboard;

public class LeaderboardEntry {
    private String username;
    private Float score;
    
    public LeaderboardEntry(String username, Float score) {
        this.username = username;
        this.score = score;
    }
    
    public LeaderboardEntry() { // Required for JSON serialization
    }
    
    public String getUsername() { 
        return username; 
    }

    public void setUsername(String username) { 
        this.username = username; 
    }

    public Float getScore() { 
        return score; 
    }
    
    public void setScore(Float score) { 
        this.score = score; 
    }
}