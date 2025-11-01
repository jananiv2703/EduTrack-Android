package com.school.edutrack.model;

public class FeePaymentRequest {
    private String student_id;
    private int fee_structure_id;
    private String status;
    private String transaction_id;

    public FeePaymentRequest(String studentId, int feeStructureId, String status, String transactionId) {
        this.student_id = studentId;
        this.fee_structure_id = feeStructureId;
        this.status = status;
        this.transaction_id = transactionId;
    }

    // Getters and Setters
    public String getStudent_id() { return student_id; }
    public void setStudent_id(String student_id) { this.student_id = student_id; }
    public int getFee_structure_id() { return fee_structure_id; }
    public void setFee_structure_id(int fee_structure_id) { this.fee_structure_id = fee_structure_id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransaction_id() { return transaction_id; }
    public void setTransaction_id(String transaction_id) { this.transaction_id = transaction_id; }
}