package com.kaio.cobra3d.model.entity;

public class Content {

    private long id;
    private String category;
    private String title;
    private String body;
    private String imageResName;
    private int displayOrder;

    public Content() {}

    public Content(String category, String title, String body, String imageResName, int displayOrder) {
        this.category = category;
        this.title = title;
        this.body = body;
        this.imageResName = imageResName;
        this.displayOrder = displayOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getImageResName() { return imageResName; }
    public void setImageResName(String imageResName) { this.imageResName = imageResName; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
