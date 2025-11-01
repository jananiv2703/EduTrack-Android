package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;

public class LeaveRequest {
    @SerializedName("id")
    private int id;

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

    @SerializedName("student_name")
    private String studentName;

    @SerializedName("class")
    private String className;

    @SerializedName("section")
    private String section;

    @SerializedName("teacher_id")
    private String teacherId;

    // Constructor
    public LeaveRequest(int id, String studentId, String leaveFrom, String leaveTo, String reason, String proof,
                        String status, String studentName, String className, String section, String teacherId) {
        this.id = id;
        this.studentId = studentId;
        this.leaveFrom = leaveFrom;
        this.leaveTo = leaveTo;
        this.reason = reason;
        this.proof = proof;
        this.status = status;
        this.studentName = studentName;
        this.className = className;
        this.section = section;
        this.teacherId = teacherId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLeaveFrom() {
        return leaveFrom;
    }

    public void setLeaveFrom(String leaveFrom) {
        this.leaveFrom = leaveFrom;
    }

    public String getLeaveTo() {
        return leaveTo;
    }

    public void setLeaveTo(String leaveTo) {
        this.leaveTo = leaveTo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", leaveFrom='" + leaveFrom + '\'' +
                ", leaveTo='" + leaveTo + '\'' +
                ", reason='" + reason + '\'' +
                ", proof='" + proof + '\'' +
                ", status='" + status + '\'' +
                ", studentName='" + studentName + '\'' +
                ", className='" + className + '\'' +
                ", section='" + section + '\'' +
                ", teacherId='" + teacherId + '\'' +
                '}';
    }
}