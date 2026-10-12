package com.serpenteando.cobra3d.model.entity;

public class Content {

    private long id;
    private String category;
    private String title;
    private String scientificName;
    private String family;
    private boolean venomous;
    private String body;
    private String habitat;
    private String distribution;
    private String diet;
    private String reproduction;
    private String medicalImportance;
    private String curiosity;
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

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }

    public boolean isVenomous() { return venomous; }
    public void setVenomous(boolean venomous) { this.venomous = venomous; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getHabitat() { return habitat; }
    public void setHabitat(String habitat) { this.habitat = habitat; }

    public String getDistribution() { return distribution; }
    public void setDistribution(String distribution) { this.distribution = distribution; }

    public String getDiet() { return diet; }
    public void setDiet(String diet) { this.diet = diet; }

    public String getReproduction() { return reproduction; }
    public void setReproduction(String reproduction) { this.reproduction = reproduction; }

    public String getMedicalImportance() { return medicalImportance; }
    public void setMedicalImportance(String medicalImportance) { this.medicalImportance = medicalImportance; }

    public String getCuriosity() { return curiosity; }
    public void setCuriosity(String curiosity) { this.curiosity = curiosity; }

    public String getImageResName() { return imageResName; }
    public void setImageResName(String imageResName) { this.imageResName = imageResName; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}