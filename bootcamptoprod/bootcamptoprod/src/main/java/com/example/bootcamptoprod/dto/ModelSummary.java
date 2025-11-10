package com.example.bootcamptoprod.dto;

public class ModelSummary {

    private String id;
    private String name;

    public ModelSummary() {
    }

    public ModelSummary(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
