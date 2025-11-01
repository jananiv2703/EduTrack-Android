package com.school.edutrack.ui.student;

import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentQuizModels;
import java.util.List;

public class SubmittedResponseAdapter extends RecyclerView.Adapter<SubmittedResponseAdapter.SubmittedResponseViewHolder> {

    private List<StudentQuizModels.StudentSubmittedResponse.SubmittedResponse> submittedResponseList;

    public SubmittedResponseAdapter(List<StudentQuizModels.StudentSubmittedResponse.SubmittedResponse> submittedResponseList) {
        this.submittedResponseList = submittedResponseList;
    }

    @NonNull
    @Override
    public SubmittedResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_submitted_response, parent, false);
        return new SubmittedResponseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmittedResponseViewHolder holder, int position) {
        StudentQuizModels.StudentSubmittedResponse.SubmittedResponse response = submittedResponseList.get(position);
        holder.quizNameTextView.setText(response.getQuizName());

        int score = response.getScore();
        int totalQuestions = response.getTotalQuestions();
        holder.scoreTextView.setText(score + "/" + totalQuestions);

        // Animate the progress bar
        int percentage = totalQuestions > 0 ? (score * 100) / totalQuestions : 0;
        ValueAnimator animator = ValueAnimator.ofInt(0, percentage);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            holder.scoreProgress.setProgress((int) animation.getAnimatedValue());
        });
        animator.start();

        holder.reviewAnswersButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Review Answers for " + response.getQuizName() + " (Feature Coming Soon)", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return submittedResponseList.size();
    }

    static class SubmittedResponseViewHolder extends RecyclerView.ViewHolder {
        TextView quizNameTextView;
        TextView scoreTextView;
        ProgressBar scoreProgress;
        Button reviewAnswersButton;

        SubmittedResponseViewHolder(@NonNull View itemView) {
            super(itemView);
            quizNameTextView = itemView.findViewById(R.id.submitted_quiz_name);
            scoreTextView = itemView.findViewById(R.id.score_text);
            scoreProgress = itemView.findViewById(R.id.score_progress);
            reviewAnswersButton = itemView.findViewById(R.id.review_answers_button);
        }
    }
}