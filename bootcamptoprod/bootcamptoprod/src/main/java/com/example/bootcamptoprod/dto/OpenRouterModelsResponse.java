package com.example.bootcamptoprod.dto;

import java.util.List;

public class OpenRouterModelsResponse {

    private List<OpenRouterModel> data;

    public OpenRouterModelsResponse() {}

    public OpenRouterModelsResponse(List<OpenRouterModel> data) {
        this.data = data;
    }

    public List<OpenRouterModel> getData() {
        return data;
    }

    public void setData(List<OpenRouterModel> data) {
        this.data = data;
    }
}
