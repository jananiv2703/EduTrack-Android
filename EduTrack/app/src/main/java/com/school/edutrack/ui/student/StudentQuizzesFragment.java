package com.school.edutrack.ui.student;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentQuiz;
import com.school.edutrack.model.StudentQuizModels;
import com.school.edutrack.network.RetrofitClient;
import com.school.edutrack.network.StudentApiService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentQuizzesFragment extends Fragment implements StudentQuizAdapter.OnStudentQuizClickListener {

    private static final String TAG = "StudentQuizzesFragment";
    private static final int VIBRATE_PERMISSION_CODE = 100;
    private String studentId;
    private String studentClass, studentSection;
    private RecyclerView studentQuizzesRecyclerView;
    private RecyclerView submittedResponsesRecyclerView;
    private ProgressBar studentQuizzesProgressBar;
    private TextView noQuizzesText, noSubmittedResponsesText;
    private StudentQuizAdapter studentQuizAdapter;
    private SubmittedResponseAdapter submittedResponseAdapter;
    private List<StudentQuiz> studentQuizList;
    private List<StudentQuizModels.StudentSubmittedResponse.SubmittedResponse> submittedResponseList;
    private StudentApiService studentApiService;

    // Timer and tab switch variables
    private CountDownTimer quizTimer;
    private Dialog quizDialog;
    private ProgressBar timerProgressBar;
    private TextView timerTextView;
    private boolean isQuizActive = false;
    private Vibrator vibrator;
    private boolean hasVibrated = false;
    private int tabSwitchCount = 0;

    private StudentQuiz currentQuiz; // Store the current quiz for use in timer and tab switch

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getString("studentId");
            Log.d(TAG, "Student ID retrieved at 01:27 PM IST, Friday, June 13, 2025: " + studentId);
        }
        studentApiService = RetrofitClient.getStudentApiService();
        studentQuizList = new ArrayList<>();
        submittedResponseList = new ArrayList<>();
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_student_quizzes, container, false);

        TextView studentIdDisplay = root.findViewById(R.id.student_quizzes_student_id);
        studentQuizzesRecyclerView = root.findViewById(R.id.student_quizzes_recycler_view);
        submittedResponsesRecyclerView = root.findViewById(R.id.submitted_responses_recycler_view);
        studentQuizzesProgressBar = root.findViewById(R.id.student_quizzes_progress_bar);
        noQuizzesText = root.findViewById(R.id.no_quizzes_text);
        noSubmittedResponsesText = root.findViewById(R.id.no_submitted_responses_text);

        studentQuizzesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        studentQuizAdapter = new StudentQuizAdapter(studentQuizList, this);
        studentQuizzesRecyclerView.setAdapter(studentQuizAdapter);

        submittedResponsesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        submittedResponseAdapter = new SubmittedResponseAdapter(submittedResponseList);
        submittedResponsesRecyclerView.setAdapter(submittedResponseAdapter);

        if (studentId != null && !studentId.isEmpty()) {
            studentIdDisplay.setText("Student ID: " + studentId);
            fetchStudentDetails();
            fetchSubmittedResponses();
        } else {
            Log.e(TAG, "Student ID is null or empty at 01:27 PM IST, Friday, June 13, 2025");
            studentIdDisplay.setText("Student ID: Not Set");
            showErrorDialog("Error", "Student ID not found");
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.VIBRATE}, VIBRATE_PERMISSION_CODE);
        }

        return root;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VIBRATE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showErrorDialog("Permission Denied", "Vibration permission denied. Timer warning vibrations disabled.");
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        if (getActivity() == null || !isAdded()) {
            Log.e(TAG, "Cannot show dialog at 01:27 PM IST, Friday, June 13, 2025: Fragment not attached to activity");
            return;
        }
        Dialog errorDialog = new Dialog(requireContext());
        errorDialog.setContentView(R.layout.dialog_error_message);
        errorDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        errorDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView errorTitle = errorDialog.findViewById(R.id.error_title);
        TextView errorMessage = errorDialog.findViewById(R.id.error_message);
        Button errorOkButton = errorDialog.findViewById(R.id.error_ok_button);

        errorTitle.setText(title);
        errorMessage.setText(message);

        errorOkButton.setOnClickListener(v -> errorDialog.dismiss());
        errorDialog.show();
    }

    private void showSuccessDialog(String scoreMessage) {
        if (getActivity() == null || !isAdded()) {
            Log.e(TAG, "Cannot show success dialog at 01:27 PM IST, Friday, June 13, 2025: Fragment not attached to activity");
            return;
        }
        Dialog successDialog = new Dialog(requireContext());
        successDialog.setContentView(R.layout.dialog_success_message);
        successDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView successMessage = successDialog.findViewById(R.id.success_message);
        Button successOkButton = successDialog.findViewById(R.id.success_ok_button);

        successMessage.setText(scoreMessage);

        successOkButton.setOnClickListener(v -> successDialog.dismiss());
        successDialog.show();
    }

    private void fetchStudentDetails() {
        studentQuizzesProgressBar.setVisibility(View.VISIBLE);
        Call<StudentQuizModels.StudentQuizDetailsResponse> call = studentApiService.getStudentQuizDetails("get_student_details", studentId);
        call.enqueue(new Callback<StudentQuizModels.StudentQuizDetailsResponse>() {
            @Override
            public void onResponse(Call<StudentQuizModels.StudentQuizDetailsResponse> call, Response<StudentQuizModels.StudentQuizDetailsResponse> response) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    StudentQuizModels.StudentQuizDetailsResponse detailsResponse = response.body();
                    if (detailsResponse.getStatus().equals("success")) {
                        studentClass = detailsResponse.getStudent().getClassName();
                        studentSection = detailsResponse.getStudent().getSection();
                        Log.d(TAG, "Fetched student details at 01:27 PM IST, Friday, June 13, 2025 - Class: " + studentClass + ", Section: " + studentSection);
                        fetchStudentQuizzes();
                    } else {
                        Log.e(TAG, "Failed to fetch student details at 01:27 PM IST, Friday, June 13, 2025: " + detailsResponse.getMessage());
                        showErrorDialog("Error", detailsResponse.getMessage());
                    }
                } else {
                    String rawResponse = "Unknown response";
                    try {
                        if (response.errorBody() != null) {
                            rawResponse = response.errorBody().string();
                        } else if (response.raw().body() != null) {
                            rawResponse = response.raw().body().toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading raw response: ", e);
                    }
                    Log.e(TAG, "Failed to fetch student details at 01:27 PM IST, Friday, June 13, 2025 - HTTP Code: " + response.code() + ", Raw Response: " + rawResponse);
                    showErrorDialog("Error", "Error fetching student details: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<StudentQuizModels.StudentQuizDetailsResponse> call, Throwable t) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error fetching student details at 01:27 PM IST, Friday, June 13, 2025: ", t);
                showErrorDialog("Network Error", "Network error: " + t.getMessage());
            }
        });
    }

    private void fetchStudentQuizzes() {
        studentQuizzesProgressBar.setVisibility(View.VISIBLE);
        Call<StudentQuizModels.StudentQuizzesResponse> call = studentApiService.getStudentQuizzes("get_quizzes", studentId, studentClass, studentSection);
        call.enqueue(new Callback<StudentQuizModels.StudentQuizzesResponse>() {
            @Override
            public void onResponse(Call<StudentQuizModels.StudentQuizzesResponse> call, Response<StudentQuizModels.StudentQuizzesResponse> response) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    StudentQuizModels.StudentQuizzesResponse quizzesResponse = response.body();
                    if (quizzesResponse.getStatus().equals("success")) {
                        studentQuizList.clear();
                        for (StudentQuizModels.StudentQuizzesResponse.StudentQuiz quiz : quizzesResponse.getQuizzes()) {
                            List<StudentQuiz.Question> questions = new ArrayList<>();
                            for (StudentQuizModels.StudentQuizzesResponse.Question question : quiz.getQuestions()) {
                                try {
                                    int questionId = Integer.parseInt(question.getId());
                                    questions.add(new StudentQuiz.Question(
                                            questionId,
                                            question.getQuestionText(),
                                            question.getOptions().getA(),
                                            question.getOptions().getB(),
                                            question.getOptions().getC(),
                                            question.getOptions().getD()
                                    ));
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid question ID format: " + question.getId() + " at 01:27 PM IST, Friday, June 13, 2025", e);
                                    showErrorDialog("Error", "Invalid question ID format");
                                    continue;
                                }
                            }
                            studentQuizList.add(new StudentQuiz(quiz.getQuizName(), quiz.getDescription(), quiz.getCreatedAt(), questions));
                        }
                        studentQuizAdapter.notifyDataSetChanged();
                        noQuizzesText.setVisibility(studentQuizList.isEmpty() ? View.VISIBLE : View.GONE);
                        studentQuizzesRecyclerView.setVisibility(studentQuizList.isEmpty() ? View.GONE : View.VISIBLE);
                        Log.d(TAG, "Fetched " + studentQuizList.size() + " student quizzes at 01:27 PM IST, Friday, June 13, 2025");
                    } else {
                        Log.e(TAG, "Failed to fetch student quizzes at 01:27 PM IST, Friday, June 13, 2025: " + quizzesResponse.getMessage());
                        showErrorDialog("Error", quizzesResponse.getMessage());
                    }
                } else {
                    String rawResponse = "Unknown response";
                    try {
                        if (response.errorBody() != null) {
                            rawResponse = response.errorBody().string();
                        } else if (response.raw().body() != null) {
                            rawResponse = response.raw().body().toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading raw response: ", e);
                    }
                    Log.e(TAG, "Failed to fetch student quizzes at 01:27 PM IST, Friday, June 13, 2025 - HTTP Code: " + response.code() + ", Raw Response: " + rawResponse);
                    showErrorDialog("Error", "Error fetching quizzes: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<StudentQuizModels.StudentQuizzesResponse> call, Throwable t) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error fetching student quizzes at 01:27 PM IST, Friday, June 13, 2025: ", t);
                showErrorDialog("Network Error", "Network error: " + t.getMessage());
            }
        });
    }

    private void fetchSubmittedResponses() {
        studentQuizzesProgressBar.setVisibility(View.VISIBLE);
        Call<StudentQuizModels.StudentSubmittedResponse> call = studentApiService.getSubmittedResponse("get_submitted_response", studentId, "");
        call.enqueue(new Callback<StudentQuizModels.StudentSubmittedResponse>() {
            @Override
            public void onResponse(Call<StudentQuizModels.StudentSubmittedResponse> call, Response<StudentQuizModels.StudentSubmittedResponse> response) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    StudentQuizModels.StudentSubmittedResponse submittedResponse = response.body();
                    if (submittedResponse.getStatus().equals("success")) {
                        submittedResponseList.clear();
                        submittedResponseList.addAll(submittedResponse.getResponses());
                        submittedResponseAdapter.notifyDataSetChanged();
                        noSubmittedResponsesText.setVisibility(submittedResponseList.isEmpty() ? View.VISIBLE : View.GONE);
                        submittedResponsesRecyclerView.setVisibility(submittedResponseList.isEmpty() ? View.GONE : View.VISIBLE);
                        Log.d(TAG, "Fetched " + submittedResponseList.size() + " submitted responses at 01:27 PM IST, Friday, June 13, 2025");
                    } else {
                        Log.e(TAG, "Failed to fetch submitted responses at 01:27 PM IST, Friday, June 13, 2025: " + submittedResponse.getMessage());
                        showErrorDialog("Error", submittedResponse.getMessage());
                    }
                } else {
                    String rawResponse = "Unknown response";
                    try {
                        if (response.errorBody() != null) {
                            rawResponse = response.errorBody().string();
                        } else if (response.raw().body() != null) {
                            rawResponse = response.raw().body().toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading raw response: ", e);
                    }
                    Log.e(TAG, "Failed to fetch submitted responses at 01:27 PM IST, Friday, June 13, 2025 - HTTP Code: " + response.code() + ", Raw Response: " + rawResponse);
                    showErrorDialog("Error", "Error fetching submitted responses: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<StudentQuizModels.StudentSubmittedResponse> call, Throwable t) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error fetching submitted responses at 01:27 PM IST, Friday, June 13, 2025: ", t);
                showErrorDialog("Network Error", "Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onStudentQuizClick(StudentQuiz studentQuiz) {
        currentQuiz = studentQuiz; // Store the quiz being taken
        quizDialog = new Dialog(requireContext());
        quizDialog.setContentView(R.layout.dialog_student_quiz_questions);
        quizDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        quizDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        TextView dialogStudentQuizName = quizDialog.findViewById(R.id.student_dialog_quiz_name);
        timerProgressBar = quizDialog.findViewById(R.id.quiz_timer_progress);
        timerTextView = quizDialog.findViewById(R.id.quiz_timer_text);
        RecyclerView studentQuestionsRecyclerView = quizDialog.findViewById(R.id.student_questions_recycler_view);
        Button studentSubmitQuizButton = quizDialog.findViewById(R.id.student_submit_quiz_button);

        dialogStudentQuizName.setText(studentQuiz.getQuizName());
        studentQuestionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        StudentQuestionAdapter studentQuestionAdapter = new StudentQuestionAdapter(studentQuiz.getQuestions());
        studentQuestionsRecyclerView.setAdapter(studentQuestionAdapter);

        int totalQuestions = studentQuiz.getQuestions().size();
        long totalTimeMillis = totalQuestions * 60 * 1000;
        startQuizTimer(totalTimeMillis);

        studentSubmitQuizButton.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.button_click));
            Dialog confirmDialog = new Dialog(requireContext());
            confirmDialog.setContentView(R.layout.dialog_error_message);
            confirmDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            confirmDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            ImageView icon = confirmDialog.findViewById(R.id.error_icon);
            TextView title = confirmDialog.findViewById(R.id.error_title);
            TextView message = confirmDialog.findViewById(R.id.error_message);
            Button okButton = confirmDialog.findViewById(R.id.error_ok_button);

            icon.setImageResource(android.R.drawable.ic_dialog_info);
            icon.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_dark)));
            title.setText("Submit Quiz");
            message.setText("Are you sure you want to submit your quiz? You cannot change your answers after submission.");
            okButton.setText("Yes");

            Button cancelButton = new Button(requireContext());
            cancelButton.setText("No");
            cancelButton.setBackgroundResource(R.drawable.outline_button);
            cancelButton.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            cancelButton.setPadding(20, 0, 20, 0);
            cancelButton.setTextSize(14);
            cancelButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ((LinearLayout) okButton.getParent()).addView(cancelButton);

            okButton.setOnClickListener(v2 -> {
                stopQuizTimer();
                submitStudentQuiz(studentQuiz);
                if (quizDialog != null && quizDialog.isShowing()) {
                    quizDialog.dismiss();
                }
                confirmDialog.dismiss();
            });

            cancelButton.setOnClickListener(v2 -> confirmDialog.dismiss());
            confirmDialog.show();
        });

        quizDialog.setOnCancelListener(dialog -> stopQuizTimer());
        quizDialog.show();
        isQuizActive = true;
        hasVibrated = false;
        tabSwitchCount = 0;
    }

    private void startQuizTimer(long totalTimeMillis) {
        final long totalTimeSeconds = totalTimeMillis / 1000;
        final long warningThreshold = 10;
        quizTimer = new CountDownTimer(totalTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                long minutes = secondsRemaining / 60;
                long seconds = secondsRemaining % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));

                int progress = (int) ((secondsRemaining * 100) / totalTimeSeconds);
                timerProgressBar.setProgress(progress);

                if (secondsRemaining <= warningThreshold && timerProgressBar.getProgressDrawable() != getResources().getDrawable(R.drawable.circular_timer_warning)) {
                    timerProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.circular_timer_warning));
                }

                if (secondsRemaining <= warningThreshold && !hasVibrated && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                    vibrator.vibrate(500);
                    hasVibrated = true;
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("00:00");
                timerProgressBar.setProgress(0);
                Log.d(TAG, "Timer finished at 01:27 PM IST, Friday, June 13, 2025 - Auto-submitting quiz");
                if (quizDialog != null && quizDialog.isShowing()) {
                    StudentQuestionAdapter adapter = (StudentQuestionAdapter) ((RecyclerView) quizDialog.findViewById(R.id.student_questions_recycler_view)).getAdapter();
                    if (adapter != null && currentQuiz != null) {
                        List<StudentQuiz.Question> updatedQuestions = adapter.getQuestions();
                        currentQuiz.setQuestions(updatedQuestions); // Update the current quiz with the latest answers
                        submitStudentQuiz(currentQuiz);
                        showErrorDialog("Time's Up", "The test has been submitted.");
                    } else {
                        Log.e(TAG, "Failed to auto-submit quiz at 01:27 PM IST, Friday, June 13, 2025: Adapter or currentQuiz is null");
                        showErrorDialog("Error", "Failed to auto-submit quiz due to an internal error.");
                    }
                    quizDialog.dismiss();
                }
                stopQuizTimer();
            }
        }.start();
    }

    private void stopQuizTimer() {
        if (quizTimer != null) {
            quizTimer.cancel();
            quizTimer = null;
        }
        isQuizActive = false;
        hasVibrated = false;
        currentQuiz = null; // Clear the current quiz reference
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isQuizActive) {
            tabSwitchCount++;
            if (tabSwitchCount == 1) {
                showErrorDialog("Warning", "You have switched tabs. This is your first warning. The quiz will auto-submit on the next violation.");
                Log.d(TAG, "First tab switch detected at 01:27 PM IST, Friday, June 13, 2025");
            } else if (tabSwitchCount >= 2) {
                Log.d(TAG, "Second tab switch detected at 01:27 PM IST, Friday, June 13, 2025 - Auto-submitting quiz");
                if (quizDialog != null && quizDialog.isShowing()) {
                    StudentQuestionAdapter adapter = (StudentQuestionAdapter) ((RecyclerView) quizDialog.findViewById(R.id.student_questions_recycler_view)).getAdapter();
                    if (adapter != null && currentQuiz != null) {
                        List<StudentQuiz.Question> updatedQuestions = adapter.getQuestions();
                        currentQuiz.setQuestions(updatedQuestions); // Update the current quiz with the latest answers
                        submitStudentQuiz(currentQuiz);
                        showErrorDialog("Malpractice Detected", "The test has been submitted.");
                    } else {
                        Log.e(TAG, "Failed to auto-submit quiz at 01:27 PM IST, Friday, June 13, 2025: Adapter or currentQuiz is null");
                        showErrorDialog("Error", "Failed to auto-submit quiz due to an internal error.");
                    }
                    quizDialog.dismiss();
                }
                stopQuizTimer();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void submitStudentQuiz(StudentQuiz studentQuiz) {
        if (studentQuiz == null) {
            Log.e(TAG, "Cannot submit quiz at 01:27 PM IST, Friday, June 13, 2025: studentQuiz is null");
            showErrorDialog("Error", "Cannot submit quiz due to an internal error.");
            return;
        }

        if (getActivity() == null || !isAdded()) {
            Log.e(TAG, "Cannot submit quiz at 01:27 PM IST, Friday, June 13, 2025: Fragment not attached to activity");
            return;
        }

        Map<String, Integer> responseMap = new HashMap<>();
        for (StudentQuiz.Question question : studentQuiz.getQuestions()) {
            if (question.getSelectedOption() == -1) {
                responseMap.put(String.valueOf(question.getId()), 0);
            } else {
                responseMap.put(String.valueOf(question.getId()), question.getSelectedOption());
            }
        }

        studentQuizzesProgressBar.setVisibility(View.VISIBLE);
        StudentQuizModels.StudentQuizSubmissionRequest request = new StudentQuizModels.StudentQuizSubmissionRequest(studentQuiz.getQuizName(), studentId, responseMap);
        Call<StudentQuizModels.StudentQuizSubmissionResponse> call = studentApiService.submitStudentQuiz("submit_quiz", request);
        call.enqueue(new Callback<StudentQuizModels.StudentQuizSubmissionResponse>() {
            @Override
            public void onResponse(Call<StudentQuizModels.StudentQuizSubmissionResponse> call, Response<StudentQuizModels.StudentQuizSubmissionResponse> response) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                if (getActivity() == null || !isAdded()) {
                    Log.e(TAG, "Cannot process quiz submission response at 01:27 PM IST, Friday, June 13, 2025: Fragment not attached to activity");
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    StudentQuizModels.StudentQuizSubmissionResponse submissionResponse = response.body();
                    if (submissionResponse.getStatus().equals("success")) {
                        String scoreMessage = "Score: " + submissionResponse.getScore() + "/" + submissionResponse.getTotalQuestions();
                        showSuccessDialog(scoreMessage);
                        fetchStudentQuizzes();
                        fetchSubmittedResponses();
                        Log.d(TAG, "Student quiz submitted successfully at 01:27 PM IST, Friday, June 13, 2025 - Score: " + submissionResponse.getScore() + "/" + submissionResponse.getTotalQuestions());
                    } else {
                        Log.e(TAG, "Failed to submit student quiz at 01:27 PM IST, Friday, June 13, 2025: " + submissionResponse.getMessage());
                        showErrorDialog("Error", submissionResponse.getMessage());
                    }
                } else {
                    String rawResponse = "Unknown response";
                    try {
                        if (response.errorBody() != null) {
                            rawResponse = response.errorBody().string();
                        } else if (response.raw().body() != null) {
                            rawResponse = response.raw().body().toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading raw response: ", e);
                    }
                    Log.e(TAG, "Failed to submit student quiz at 01:27 PM IST, Friday, June 13, 2025 - HTTP Code: " + response.code() + ", Raw Response: " + rawResponse);
                    showErrorDialog("Error", "Error submitting quiz: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<StudentQuizModels.StudentQuizSubmissionResponse> call, Throwable t) {
                studentQuizzesProgressBar.setVisibility(View.GONE);
                if (getActivity() == null || !isAdded()) {
                    Log.e(TAG, "Cannot process quiz submission failure at 01:27 PM IST, Friday, June 13, 2025: Fragment not attached to activity");
                    return;
                }
                Log.e(TAG, "Network error submitting student quiz at 01:27 PM IST, Friday, June 13, 2025: ", t);
                showErrorDialog("Network Error", "Network error: " + t.getMessage());
            }
        });
    }
}