package com.school.edutrack.model;

import java.util.List;

public class FeePaymentResponse {
    private String status;
    private List<FeePayment> data;
    private String message;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<FeePayment> getData() { return data; }
    public void setData(List<FeePayment> data) { this.data = data; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static class ApiResponse {
        private String status;
        private String message;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}