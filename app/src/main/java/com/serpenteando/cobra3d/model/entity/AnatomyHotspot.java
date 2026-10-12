package com.serpenteando.cobra3d.model.entity;

public class AnatomyHotspot {

    private long id;
    private long layerId;
    private String hotspotName;
    private String description;
    private float positionX;
    private float positionY;
    private float positionZ;

    public AnatomyHotspot() {}

    public AnatomyHotspot(long layerId, String hotspotName, String description,
                          float positionX, float positionY, float positionZ) {
        this.layerId = layerId;
        this.hotspotName = hotspotName;
        this.description = description;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getLayerId() { return layerId; }
    public void setLayerId(long layerId) { this.layerId = layerId; }

    public String getHotspotName() { return hotspotName; }
    public void setHotspotName(String hotspotName) { this.hotspotName = hotspotName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public float getPositionX() { return positionX; }
    public void setPositionX(float positionX) { this.positionX = positionX; }

    public float getPositionY() { return positionY; }
    public void setPositionY(float positionY) { this.positionY = positionY; }

    public float getPositionZ() { return positionZ; }
    public void setPositionZ(float positionZ) { this.positionZ = positionZ; }
}
