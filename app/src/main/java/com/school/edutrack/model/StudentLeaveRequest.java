package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class StudentLeaveRequest {
    @SerializedName("student_id")
    private String studentId;
    @SerializedName("leave_from")
    private String leaveFrom;
    @SerializedName("leave_to")
    private String leaveTo;
    @SerializedName("reason")
    private String reason;
    @SerializedName("proof")
    private String proof;
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("leave_requests")
    private StudentLeaveRequest[] leaveRequests;

    // Constructor for submitting a leave request
    public StudentLeaveRequest(String studentId, String leaveFrom, String leaveTo, String reason, String proof) {
        this.studentId = studentId;
        this.leaveFrom = leaveFrom;
        this.leaveTo = leaveTo;
        this.reason = reason;
        this.proof = proof;
    }

    // Getters and setters
    public String getStudent_id() { return studentId; }
    public void setStudent_id(String studentId) { this.studentId = studentId; }
    public String getLeave_from() { return leaveFrom; }
    public void setLeave_from(String leaveFrom) { this.leaveFrom = leaveFrom; }
    public String getLeave_to() { return leaveTo; }
    public void setLeave_to(String leaveTo) { this.leaveTo = leaveTo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getProof() { return proof; }
    public void setProof(String proof) { this.proof = proof; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public StudentLeaveRequest[] getLeave_requests() { return leaveRequests; }
    public void setLeave_requests(StudentLeaveRequest[] leaveRequests) { this.leaveRequests = leaveRequests; }
}