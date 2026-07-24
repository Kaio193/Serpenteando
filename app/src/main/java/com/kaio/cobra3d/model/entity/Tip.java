package com.kaio.cobra3d.model.entity;

public class Tip {

    private long id;
    private String tipText;
    private String category;
    private boolean isActive;

    public Tip() {}

    public Tip(String tipText, String category) {
        this.tipText = tipText;
        this.category = category;
        this.isActive = true;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTipText() { return tipText; }
    public void setTipText(String tipText) { this.tipText = tipText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
