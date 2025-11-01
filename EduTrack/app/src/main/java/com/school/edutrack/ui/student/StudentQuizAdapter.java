package com.school.edutrack.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentQuiz;

import java.util.List;

public class StudentQuizAdapter extends RecyclerView.Adapter<StudentQuizAdapter.StudentQuizViewHolder> {
    private List<StudentQuiz> studentQuizList;
    private OnStudentQuizClickListener listener;

    public interface OnStudentQuizClickListener {
        void onStudentQuizClick(StudentQuiz studentQuiz);
    }

    public StudentQuizAdapter(List<StudentQuiz> studentQuizList, OnStudentQuizClickListener listener) {
        this.studentQuizList = studentQuizList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentQuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_quiz, parent, false);
        return new StudentQuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentQuizViewHolder holder, int position) {
        StudentQuiz studentQuiz = studentQuizList.get(position);
        holder.studentQuizName.setText(studentQuiz.getQuizName());
        holder.studentQuizDescription.setText(studentQuiz.getDescription());
        holder.studentQuizCreatedAt.setText("Created At: " + studentQuiz.getCreatedAt());
        holder.studentTakeQuizButton.setOnClickListener(v -> listener.onStudentQuizClick(studentQuiz));
    }

    @Override
    public int getItemCount() {
        return studentQuizList.size();
    }

    static class StudentQuizViewHolder extends RecyclerView.ViewHolder {
        TextView studentQuizName, studentQuizDescription, studentQuizCreatedAt;
        Button studentTakeQuizButton;

        public StudentQuizViewHolder(@NonNull View itemView) {
            super(itemView);
            studentQuizName = itemView.findViewById(R.id.student_quiz_name);
            studentQuizDescription = itemView.findViewById(R.id.student_quiz_description);
            studentQuizCreatedAt = itemView.findViewById(R.id.student_quiz_created_at);
            studentTakeQuizButton = itemView.findViewById(R.id.student_take_quiz_button);
        }
    }
}