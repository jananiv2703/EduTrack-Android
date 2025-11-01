package com.school.edutrack.model;

public class StudentLogin {
    private String message;
    private boolean pin_set;
    private User user;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPinSet() {
        return pin_set;
    }

    public void setPinSet(boolean pin_set) {
        this.pin_set = pin_set;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class User {
        private int id;
        private String student_id;
        private String name;
        private String email;
        private String className;
        private String section;
        private String admission_no;
        private String register_no;
        private String dob;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getStudentId() {
            return student_id;
        }

        public void setStudentId(String student_id) {
            this.student_id = student_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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

        public String getAdmissionNo() {
            return admission_no;
        }

        public void setAdmissionNo(String admission_no) {
            this.admission_no = admission_no;
        }

        public String getRegisterNo() {
            return register_no;
        }

        public void setRegisterNo(String register_no) {
            this.register_no = register_no;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }
    }
}