package com.example.bootcamptoprod.dto;

public class OpenRouterModel {
    private String id;
    private String name;
    private String description;

    public OpenRouterModel() {}

    public OpenRouterModel(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
