package com.serpenteando.cobra3d.model.entity;

public class MythTruth {

    private long id;
    private String statement;
    private boolean isMyth;
    private String explanation;
    private String category;

    public MythTruth() {}

    public MythTruth(String statement, boolean isMyth, String explanation, String category) {
        this.statement = statement;
        this.isMyth = isMyth;
        this.explanation = explanation;
        this.category = category;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public boolean isMyth() { return isMyth; }
    public void setMyth(boolean myth) { isMyth = myth; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
