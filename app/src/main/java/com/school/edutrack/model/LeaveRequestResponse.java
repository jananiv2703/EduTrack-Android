package com.school.edutrack.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LeaveRequestResponse {
    @SerializedName("leave_requests")
    private List<LeaveRequest> leaveRequests;

    @SerializedName("message")
    private String message;

    public List<LeaveRequest> getLeaveRequests() {
        return leaveRequests;
    }

    public void setLeaveRequests(List<LeaveRequest> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "LeaveRequestResponse{" +
                "leaveRequests=" + leaveRequests +
                ", message='" + message + '\'' +
                '}';
    }
}