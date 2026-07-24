package com.kaio.cobra3d.model.entity;

public class MissionItem {

    private long id;
    private long missionId;
    private String itemText;
    private boolean isCorrect;
    private int displayOrder;

    public MissionItem() {}

    public MissionItem(long missionId, String itemText, boolean isCorrect, int displayOrder) {
        this.missionId = missionId;
        this.itemText = itemText;
        this.isCorrect = isCorrect;
        this.displayOrder = displayOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMissionId() { return missionId; }
    public void setMissionId(long missionId) { this.missionId = missionId; }

    public String getItemText() { return itemText; }
    public void setItemText(String itemText) { this.itemText = itemText; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
