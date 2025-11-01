package com.school.edutrack.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.QuizResponse;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<QuizResponse> quizzes;
    private final OnQuizClickListener listener;

    public interface OnQuizClickListener {
        void onQuizClick(String quizName);
    }

    public QuizAdapter(List<QuizResponse> quizzes, OnQuizClickListener listener) {
        this.quizzes = quizzes;
        this.listener = listener;
    }

    public void updateQuizzes(List<QuizResponse> newQuizzes) {
        this.quizzes = newQuizzes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizResponse quiz = quizzes.get(position);
        holder.quizNameTextView.setText(quiz.getQuiz().getQuizName());
        holder.descriptionTextView.setText(quiz.getQuiz().getDescription());
        holder.itemView.setOnClickListener(v -> listener.onQuizClick(quiz.getQuiz().getQuizName()));
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView quizNameTextView, descriptionTextView;

        QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            quizNameTextView = itemView.findViewById(R.id.quizNameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}