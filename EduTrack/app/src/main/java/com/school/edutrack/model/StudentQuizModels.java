package com.school.edutrack.model;

import java.util.List;
import java.util.Map;

public class StudentQuizModels {

    // Response model for fetching student details
    public static class StudentQuizDetailsResponse {
        private String status;
        private Student student;
        private String message;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Student getStudent() { return student; }
        public void setStudent(Student student) { this.student = student; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public static class Student {
            private String student_id;
            private String name;
            private String className;
            private String section;
            private String register_no;

            public String getStudentId() { return student_id; }
            public void setStudentId(String student_id) { this.student_id = student_id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getClassName() { return className; }
            public void setClassName(String className) { this.className = className; }
            public String getSection() { return section; }
            public void setSection(String section) { this.section = section; }
            public String getRegisterNo() { return register_no; }
            public void setRegisterNo(String register_no) { this.register_no = register_no; }
        }
    }

    // Response model for fetching quizzes
    public static class StudentQuizzesResponse {
        private String status;
        private List<StudentQuiz> quizzes;
        private String message;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<StudentQuiz> getQuizzes() { return quizzes; }
        public void setQuizzes(List<StudentQuiz> quizzes) { this.quizzes = quizzes; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public static class StudentQuiz {
            private String quiz_name;
            private String description;
            private String created_at;
            private List<Question> questions;

            public String getQuizName() { return quiz_name; }
            public void setQuizName(String quiz_name) { this.quiz_name = quiz_name; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public String getCreatedAt() { return created_at; }
            public void setCreatedAt(String created_at) { this.created_at = created_at; }
            public List<Question> getQuestions() { return questions; }
            public void setQuestions(List<Question> questions) { this.questions = questions; }
        }

        public static class Question {
            private String id;
            private String question_text;
            private Options options;

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getQuestionText() { return question_text; }
            public void setQuestionText(String question_text) { this.question_text = question_text; }
            public Options getOptions() { return options; }
            public void setOptions(Options options) { this.options = options; }
        }

        public static class Options {
            private String a;
            private String b;
            private String c;
            private String d;

            public String getA() { return a; }
            public void setA(String a) { this.a = a; }
            public String getB() { return b; }
            public void setB(String b) { this.b = b; }
            public String getC() { return c; }
            public void setC(String c) { this.c = c; }
            public String getD() { return d; }
            public void setD(String d) { this.d = d; }
        }
    }

    // Request model for submitting quiz responses
    public static class StudentQuizSubmissionRequest {
        private String quiz_name;
        private String student_id;
        private Map<String, Integer> response;

        public StudentQuizSubmissionRequest(String quiz_name, String student_id, Map<String, Integer> response) {
            this.quiz_name = quiz_name;
            this.student_id = student_id;
            this.response = response;
        }

        public String getQuizName() { return quiz_name; }
        public void setQuizName(String quiz_name) { this.quiz_name = quiz_name; }
        public String getStudentId() { return student_id; }
        public void setStudentId(String student_id) { this.student_id = student_id; }
        public Map<String, Integer> getResponse() { return response; }
        public void setResponse(Map<String, Integer> response) { this.response = response; }
    }

    // Response model for quiz submission
    public static class StudentQuizSubmissionResponse {
        private String status;
        private String message;
        private int score;
        private int total_questions;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getTotalQuestions() { return total_questions; }
        public void setTotalQuestions(int total_questions) { this.total_questions = total_questions; }
    }

    // New response model for fetching submitted quiz responses
    public static class StudentSubmittedResponse {
        private String status;
        private List<SubmittedResponse> responses;
        private String message;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<SubmittedResponse> getResponses() { return responses; }
        public void setResponses(List<SubmittedResponse> responses) { this.responses = responses; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public static class SubmittedResponse {
            private String quiz_name;
            private String student_reg_number;
            private String student_name;
            private String className;
            private String section;
            private Map<String, Integer> response;
            private int score;
            private int total_questions;

            public String getQuizName() { return quiz_name; }
            public void setQuizName(String quiz_name) { this.quiz_name = quiz_name; }
            public String getStudentRegNumber() { return student_reg_number; }
            public void setStudentRegNumber(String student_reg_number) { this.student_reg_number = student_reg_number; }
            public String getStudentName() { return student_name; }
            public void setStudentName(String student_name) { this.student_name = student_name; }
            public String getClassName() { return className; }
            public void setClassName(String className) { this.className = className; }
            public String getSection() { return section; }
            public void setSection(String section) { this.section = section; }
            public Map<String, Integer> getResponse() { return response; }
            public void setResponse(Map<String, Integer> response) { this.response = response; }
            public int getScore() { return score; }
            public void setScore(int score) { this.score = score; }
            public int getTotalQuestions() { return total_questions; }
            public void setTotalQuestions(int total_questions) { this.total_questions = total_questions; }
        }
    }
}