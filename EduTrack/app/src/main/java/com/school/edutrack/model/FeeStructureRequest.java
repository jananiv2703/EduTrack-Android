package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class FeeStructureRequest {
    @SerializedName("id")
    private int id; // Used for PUT
    @SerializedName("fee_type")
    private String feeType;
    @SerializedName("class")
    private String className;
    @SerializedName("amount")
    private double amount; // Matches PHP validation expecting a number
    @SerializedName("deadline")
    private String deadline;

    public FeeStructureRequest(String feeType, String className, double amount, String deadline) {
        this.feeType = feeType;
        this.className = className;
        this.amount = amount;
        this.deadline = deadline;
    }

    public FeeStructureRequest(int id, String feeType, String className, double amount, String deadline) {
        this.id = id;
        this.feeType = feeType;
        this.className = className;
        this.amount = amount;
        this.deadline = deadline;
    }
}