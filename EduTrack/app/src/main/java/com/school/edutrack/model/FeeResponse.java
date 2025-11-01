package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Response for GET requests to fetch fee structures
public class FeeResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private List<FeeStructure> data;

    public String getStatus() {
        return status;
    }

    public List<FeeStructure> getData() {
        return data;
    }
}