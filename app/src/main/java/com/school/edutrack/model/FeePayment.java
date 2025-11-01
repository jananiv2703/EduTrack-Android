package com.school.edutrack.model;

public class FeePayment {
    private int id;
    private String feeType;
    private String className;
    private String amount;
    private String deadline;
    private String status;
    private String transactionId;
    private String paymentCreatedAt;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getPaymentCreatedAt() { return paymentCreatedAt; }
    public void setPaymentCreatedAt(String paymentCreatedAt) { this.paymentCreatedAt = paymentCreatedAt; }
}