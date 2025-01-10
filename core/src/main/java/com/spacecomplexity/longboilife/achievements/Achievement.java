package com.spacecomplexity.longboilife.achievements;

public class Achievement {
    private final String id;
    private final String title;
    private final String description;
    private boolean unlocked;
    private boolean failed;
    private final AchievementType type;

    public enum AchievementType {
        BUILDING_COUNT,
        SATISFACTION_SCORE,
        BUDGET
    }
    
    public Achievement(String id, String title, String description, AchievementType type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.unlocked = false;
        this.failed = false;
        this.type = type;
    }
    
    // Getters and setters
    public String getId() { 
        return id;
    }

    public String getTitle() { 
        return title;
    }

    public String getDescription() { 
        return description;
    }

    public boolean isUnlocked() { 
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) { 
        this.unlocked = unlocked;
    }
    
    public AchievementType getType() { 
        return type;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
