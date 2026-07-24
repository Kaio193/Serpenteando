package com.kaio.cobra3d.model.entity;

public class Mission {

    private long id;
    private String title;
    private String description;
    private int difficulty;
    private int points;
    private boolean isCompleted;

    public Mission() {}

    public Mission(String title, String description, int difficulty, int points) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.points = points;
        this.isCompleted = false;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
