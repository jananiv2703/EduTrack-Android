package com.school.edutrack.ui.teacher;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.school.edutrack.R;
import com.school.edutrack.model.ApiResponse;
import com.school.edutrack.model.Quiz;
import com.school.edutrack.model.QuizResponse;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.utils.DialogUtils;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherConductQuizFragment extends Fragment {

    private String teacherId;
    private LinearLayout questionsContainer;
    private List<View> questionViews;
    private ProgressBar loadingSpinner;
    private ConstraintLayout quizDetailsContainer;
    private TextView quizDetailsText;
    private TextView questionsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_conduct_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve teacherId from arguments
        if (getArguments() != null) {
            teacherId = getArguments().getString("teacherId");
            if (teacherId == null || teacherId.isEmpty()) {
                Log.d("TeacherConductQuiz", "Teacher ID is null or empty");
                DialogUtils.showFailureDialog(requireContext(), "Error", "Teacher ID is missing", null);
                return;
            }
            Log.d("TeacherConductQuiz", "Teacher ID retrieved: " + teacherId);
        } else {
            Log.d("TeacherConductQuiz", "Arguments bundle is null");
            DialogUtils.showFailureDialog(requireContext(), "Error", "Teacher ID not found", null);
            return;
        }

        // Initialize UI elements
        TextView teacherIdDisplay = view.findViewById(R.id.teacher_id_display);
        MaterialAutoCompleteTextView classInput = view.findViewById(R.id.class_input);
        MaterialAutoCompleteTextView sectionInput = view.findViewById(R.id.section_input);
        TextInputEditText quizNameInput = view.findViewById(R.id.quiz_name_input);
        TextInputEditText descriptionInput = view.findViewById(R.id.description_input);
        questionsContainer = view.findViewById(R.id.questions_container);
        Button addQuestionButton = view.findViewById(R.id.add_question_button);
        Button createQuizButton = view.findViewById(R.id.create_quiz_button);
        TextInputEditText viewQuizNameInput = view.findViewById(R.id.view_quiz_name_input);
        Button viewQuizButton = view.findViewById(R.id.view_quiz_button);
        quizDetailsContainer = view.findViewById(R.id.quiz_details_container);
        quizDetailsText = view.findViewById(R.id.quiz_details_text);
        questionsText = view.findViewById(R.id.questions_text);
        loadingSpinner = view.findViewById(R.id.loading_spinner);

        // Validate view initialization
        if (teacherIdDisplay == null || classInput == null || sectionInput == null || quizNameInput == null ||
                descriptionInput == null || questionsContainer == null || addQuestionButton == null ||
                createQuizButton == null || viewQuizNameInput == null || viewQuizButton == null ||
                quizDetailsContainer == null || quizDetailsText == null || questionsText == null ||
                loadingSpinner == null) {
            Log.d("TeacherConductQuiz", "One or more views are null");
            DialogUtils.showFailureDialog(requireContext(), "Error", "UI initialization failed. Please restart the app.", null);
            return;
        }

        // Set up dropdowns
        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.class_options, android.R.layout.simple_dropdown_item_1line);
        classInput.setAdapter(classAdapter);

        ArrayAdapter<CharSequence> sectionAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.section_options, android.R.layout.simple_dropdown_item_1line);
        sectionInput.setAdapter(sectionAdapter);

        questionViews = new ArrayList<>();

        teacherIdDisplay.setText("Teacher ID: " + teacherId);

        // Add Question Button
        addQuestionButton.setOnClickListener(v -> addQuestionView());

        // Create Quiz Button
        createQuizButton.setOnClickListener(v -> {
            String className = classInput.getText().toString().trim();
            String section = sectionInput.getText().toString().trim();
            String quizName = quizNameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            if (className.isEmpty() || section.isEmpty() || quizName.isEmpty() || description.isEmpty() || questionViews.isEmpty()) {
                DialogUtils.showFailureDialog(requireContext(), "Error", "Please fill all fields and add at least one question", null);
                return;
            }

            List<Quiz.Question> questions = new ArrayList<>();
            for (View questionView : questionViews) {
                TextInputEditText questionTextInput = questionView.findViewById(R.id.question_text_input);
                TextInputEditText optionAInput = questionView.findViewById(R.id.option_a_input);
                TextInputEditText optionBInput = questionView.findViewById(R.id.option_b_input);
                TextInputEditText optionCInput = questionView.findViewById(R.id.option_c_input);
                TextInputEditText optionDInput = questionView.findViewById(R.id.option_d_input);
                TextInputEditText correctOptionInput = questionView.findViewById(R.id.correct_option_input);

                if (questionTextInput == null || optionAInput == null || optionBInput == null ||
                        optionCInput == null || optionDInput == null || correctOptionInput == null) {
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Question view initialization failed", null);
                    return;
                }

                String questionText = questionTextInput.getText().toString().trim();
                String optionA = optionAInput.getText().toString().trim();
                String optionB = optionBInput.getText().toString().trim();
                String optionC = optionCInput.getText().toString().trim();
                String optionD = optionDInput.getText().toString().trim();
                String correctOptionStr = correctOptionInput.getText().toString().trim();

                if (questionText.isEmpty() || optionA.isEmpty() || optionB.isEmpty() || optionC.isEmpty() ||
                        optionD.isEmpty() || correctOptionStr.isEmpty()) {
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Please fill all fields for each question", null);
                    return;
                }

                int correctOption;
                try {
                    correctOption = Integer.parseInt(correctOptionStr);
                    if (correctOption < 0 || correctOption > 3) {
                        DialogUtils.showFailureDialog(requireContext(), "Error", "Correct option must be between 0 and 3", null);
                        return;
                    }
                } catch (NumberFormatException e) {
                    DialogUtils.showFailureDialog(requireContext(), "Error", "Correct option must be a number between 0 and 3", null);
                    return;
                }

                Quiz.Question.Options options = new Quiz.Question.Options(optionA, optionB, optionC, optionD);
                Quiz.Question question = new Quiz.Question(questionText, options, correctOption);
                questions.add(question);
            }

            Quiz quiz = new Quiz(teacherId, className, section, quizName, description, questions);
            createQuiz(quiz);
        });

        // View Quiz Button
        viewQuizButton.setOnClickListener(v -> {
            String quizName = viewQuizNameInput.getText().toString().trim();
            if (quizName.isEmpty()) {
                DialogUtils.showFailureDialog(requireContext(), "Error", "Please enter a quiz name to view", null);
                return;
            }
            getQuizDetails(quizName);
        });
    }

    private void addQuestionView() {
        if (getContext() == null) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Fragment not attached to context", null);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View questionView = inflater.inflate(R.layout.quiz_input_layout, questionsContainer, false);

        int index = questionViews.size();
        questionView.setTag(index);
        TextView questionNumberText = questionView.findViewById(R.id.question_number_text);
        if (questionNumberText != null) {
            questionNumberText.setText("Question " + (index + 1));
        }

        Button removeButton = questionView.findViewById(R.id.remove_question_button);
        if (removeButton != null) {
            removeButton.setOnClickListener(v -> {
                questionsContainer.removeView(questionView);
                questionViews.remove(questionView);
                updateQuestionNumbers();
            });
        }

        questionsContainer.addView(questionView);
        questionViews.add(questionView);
        updateQuestionNumbers();
    }

    private void updateQuestionNumbers() {
        for (int i = 0; i < questionViews.size(); i++) {
            View questionView = questionViews.get(i);
            if (questionView != null) {
                TextView questionNumberText = questionView.findViewById(R.id.question_number_text);
                if (questionNumberText != null) {
                    questionNumberText.setText("Question " + (i + 1));
                }
            }
        }
    }

    private void createQuiz(Quiz quiz) {
        DialogUtils.showLoadingDialog(requireContext());
        RetrofitClient.getApiService().createQuiz(quiz).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                        DialogUtils.showSuccessDialog(requireContext(), "Success", apiResponse.getMessage(), () -> resetForm());
                    } else {
                        DialogUtils.showFailureDialog(requireContext(), "Error", apiResponse.getMessage(), null);
                    }
                    Log.d("TeacherConductQuiz", "Create Quiz Response at " + java.time.LocalDateTime.now() + ": " + new Gson().toJson(apiResponse));
                } else {
                    String errorMsg = "Failed to create quiz: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("TeacherConductQuiz", "Error parsing error body", e);
                        }
                    }
                    DialogUtils.showFailureDialog(requireContext(), "Error", errorMsg, null);
                    Log.e("TeacherConductQuiz", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                String errorMsg = "Error creating quiz: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
                DialogUtils.showFailureDialog(requireContext(), "Error", errorMsg, null);
                Log.e("TeacherConductQuiz", errorMsg, t);
            }
        });
    }

    private void getQuizDetails(String quizName) {
        DialogUtils.showLoadingDialog(requireContext());
        quizDetailsContainer.setVisibility(View.GONE);

        RetrofitClient.getApiService().getQuizDetails(quizName).enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                DialogUtils.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    QuizResponse quizResponse = response.body();
                    displayQuizDetails(quizResponse);
                    Log.d("TeacherConductQuiz", "Get Quiz Details Response at " + java.time.LocalDateTime.now() + ": " + new Gson().toJson(quizResponse));
                } else {
                    String errorMsg = "Failed to fetch quiz details: HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("TeacherConductQuiz", "Error parsing error body", e);
                        }
                    }
                    DialogUtils.showFailureDialog(requireContext(), "Error", errorMsg, null);
                    Log.e("TeacherConductQuiz", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<QuizResponse> call, Throwable t) {
                DialogUtils.hideLoadingDialog();
                String errorMsg = "Error fetching quiz details: " + (t.getMessage() != null ? t.getMessage() : "Unknown error");
                DialogUtils.showFailureDialog(requireContext(), "Error", errorMsg, null);
                Log.e("TeacherConductQuiz", errorMsg, t);
            }
        });
    }

    private void displayQuizDetails(QuizResponse quizResponse) {
        quizDetailsContainer.setVisibility(View.VISIBLE);

        QuizResponse.Quiz quiz = quizResponse.getQuiz();
        if (quiz == null) {
            DialogUtils.showFailureDialog(requireContext(), "Error", "Quiz data is missing in the response", null);
            quizDetailsText.setText("No quiz data available");
            questionsText.setText("");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Quiz Name: ").append(quiz.getQuizName() != null ? quiz.getQuizName() : "N/A").append("\n")
                .append("Class: ").append(quiz.getClassName() != null ? quiz.getClassName() : "N/A").append("\n")
                .append("Section: ").append(quiz.getSection() != null ? quiz.getSection() : "N/A").append("\n")
                .append("Description: ").append(quiz.getDescription() != null ? quiz.getDescription() : "N/A").append("\n")
                .append("Created At: ").append(quiz.getCreatedAt() != null ? quiz.getCreatedAt() : "N/A");
        quizDetailsText.setText(details.toString());

        List<QuizResponse.Question> questions = quizResponse.getQuestions();
        if (questions == null || questions.isEmpty()) {
            questionsText.setText("No questions available");
            return;
        }

        StringBuilder questionsBuilder = new StringBuilder("Questions:\n");
        for (QuizResponse.Question question : questions) {
            if (question != null) {
                QuizResponse.Question.Options options = question.getOptions();
                questionsBuilder.append("Q").append(question.getId()).append(": ")
                        .append(question.getQuestionText() != null ? question.getQuestionText() : "N/A").append("\n")
                        .append("A: ").append(options != null && options.getOptionA() != null ? options.getOptionA() : "N/A").append("\n")
                        .append("B: ").append(options != null && options.getOptionB() != null ? options.getOptionB() : "N/A").append("\n")
                        .append("C: ").append(options != null && options.getOptionC() != null ? options.getOptionC() : "N/A").append("\n")
                        .append("D: ").append(options != null && options.getOptionD() != null ? options.getOptionD() : "N/A").append("\n\n");
            }
        }
        questionsText.setText(questionsBuilder.toString());
    }

    private void resetForm() {
        MaterialAutoCompleteTextView classInput = getView().findViewById(R.id.class_input);
        MaterialAutoCompleteTextView sectionInput = getView().findViewById(R.id.section_input);
        TextInputEditText quizNameInput = getView().findViewById(R.id.quiz_name_input);
        TextInputEditText descriptionInput = getView().findViewById(R.id.description_input);
        TextInputEditText viewQuizNameInput = getView().findViewById(R.id.view_quiz_name_input);

        if (classInput != null) classInput.setText("");
        if (sectionInput != null) sectionInput.setText("");
        if (quizNameInput != null) quizNameInput.setText("");
        if (descriptionInput != null) descriptionInput.setText("");
        if (viewQuizNameInput != null) viewQuizNameInput.setText("");
        questionsContainer.removeAllViews();
        questionViews.clear();
        quizDetailsContainer.setVisibility(View.GONE);
    }
}