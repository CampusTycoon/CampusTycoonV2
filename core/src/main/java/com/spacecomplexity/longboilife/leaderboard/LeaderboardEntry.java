package com.spacecomplexity.longboilife.leaderboard;

public class LeaderboardEntry {
    private String username;
    private int score;
    
    public LeaderboardEntry(String username, int score) {
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

    public int getScore() { 
        return score; 
    }
    
    public void setScore(int score) { 
        this.score = score; 
    }
}