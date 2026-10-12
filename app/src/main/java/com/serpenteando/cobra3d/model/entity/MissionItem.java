package com.serpenteando.cobra3d.model.entity;

public class MissionItem {

    private long id;
    private long missionId;
    private String itemText;
    private String trigger;
    private int target;
    private boolean isCorrect;
    private int displayOrder;

    public MissionItem() {}

    public MissionItem(long missionId, String itemText, String trigger, int target,
                       boolean isCorrect, int displayOrder) {
        this.missionId = missionId;
        this.itemText = itemText;
        this.trigger = trigger;
        this.target = target;
        this.isCorrect = isCorrect;
        this.displayOrder = displayOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getMissionId() { return missionId; }
    public void setMissionId(long missionId) { this.missionId = missionId; }

    public String getItemText() { return itemText; }
    public void setItemText(String itemText) { this.itemText = itemText; }

    public String getTrigger() { return trigger; }
    public void setTrigger(String trigger) { this.trigger = trigger; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
