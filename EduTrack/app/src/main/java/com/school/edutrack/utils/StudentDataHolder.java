package com.school.edutrack.utils;

public class StudentDataHolder {
    private static StudentDataHolder instance;
    private String studentClass;
    private String section;

    private StudentDataHolder() {
        // Private constructor to prevent instantiation
    }

    public static synchronized StudentDataHolder getInstance() {
        if (instance == null) {
            instance = new StudentDataHolder();
        }
        return instance;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    // Optional: Clear data when the user logs out
    public void clearData() {
        studentClass = null;
        section = null;
    }
}