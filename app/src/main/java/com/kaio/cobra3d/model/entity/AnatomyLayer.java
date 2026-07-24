package com.kaio.cobra3d.model.entity;

public class AnatomyLayer {

    private long id;
    private String layerName;
    private String displayName;
    private String description;
    private String modelFileName;
    private int displayOrder;

    public AnatomyLayer() {}

    public AnatomyLayer(String layerName, String displayName, String description,
                        String modelFileName, int displayOrder) {
        this.layerName = layerName;
        this.displayName = displayName;
        this.description = description;
        this.modelFileName = modelFileName;
        this.displayOrder = displayOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getLayerName() { return layerName; }
    public void setLayerName(String layerName) { this.layerName = layerName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getModelFileName() { return modelFileName; }
    public void setModelFileName(String modelFileName) { this.modelFileName = modelFileName; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
