package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class FeeStructure {
    @SerializedName("id")
    private int id;
    @SerializedName("fee_type")
    private String feeType;
    @SerializedName("class")
    private String className;
    @SerializedName("amount")
    private String amount; // String to match PHP JSON output
    @SerializedName("deadline")
    private String deadline;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}