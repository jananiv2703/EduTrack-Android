package com.school.edutrack.model;

import java.util.List;

public class LearningMaterialsResponse {
    private String status;
    private List<LearningMaterial> data;

    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<LearningMaterial> getData() { return data; }
    public void setData(List<LearningMaterial> data) { this.data = data; }
}