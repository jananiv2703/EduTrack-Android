package com.school.edutrack.model;
import com.school.edutrack.model.StudentLeaveRequest;

// Model for API responses (both POST and GET)
public class StudentLeaveResponse {
    private String message; // Used in POST response and error cases
    private StudentLeaveRequest[] leave_requests; // Used in GET response

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public StudentLeaveRequest[] getLeave_requests() { return leave_requests; }
    public void setLeave_requests(StudentLeaveRequest[] leave_requests) { this.leave_requests = leave_requests; }
}