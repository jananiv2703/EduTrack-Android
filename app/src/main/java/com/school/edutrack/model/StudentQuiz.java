package com.school.edutrack.model;

import java.util.List;

public class StudentQuiz {
    private String quizName;
    private String description;
    private String createdAt;
    private List<Question> questions;

    public StudentQuiz(String quizName, String description, String createdAt, List<Question> questions) {
        this.quizName = quizName;
        this.description = description;
        this.createdAt = createdAt;
        this.questions = questions;
    }

    public String getQuizName() {
        return quizName;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> updatedQuestions) {
    }

    public static class Question {
        private int id;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private int selectedOption = -1; // -1 means no option selected

        public Question(int id, String questionText, String optionA, String optionB, String optionC, String optionD) {
            this.id = id;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
        }

        public int getId() {
            return id;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getOptionA() {
            return optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public int getSelectedOption() {
            return selectedOption;
        }

        public void setSelectedOption(int selectedOption) {
            this.selectedOption = selectedOption;
        }
    }
}