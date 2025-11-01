package com.school.edutrack.ui.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.ExamScore;
import java.util.List;

public class ExamScoreAdapter extends RecyclerView.Adapter<ExamScoreAdapter.ViewHolder> {

    private List<ExamScore> scoresList;

    public ExamScoreAdapter(List<ExamScore> scoresList) {
        this.scoresList = scoresList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExamScore score = scoresList.get(position);
        holder.studentIdTextView.setText("Student ID: " + (score.getStudentId() != null ? score.getStudentId() : "N/A"));
        holder.examNameTextView.setText("Exam: " + (score.getExamName() != null ? score.getExamName() : "N/A"));
        holder.subjectTextView.setText("Subject: " + (score.getSubject() != null ? score.getSubject() : "N/A"));
        holder.marksTextView.setText("Marks: " + (score.getMarksObtained() != null ? score.getMarksObtained() : "0") + "/" + (score.getMaxMarks() != null ? score.getMaxMarks() : "0"));
    }

    @Override
    public int getItemCount() {
        return scoresList != null ? scoresList.size() : 0;
    }

    public void updateScores(List<ExamScore> newScores) {
        scoresList.clear();
        if (newScores != null) {
            scoresList.addAll(newScores);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentIdTextView, examNameTextView, subjectTextView, marksTextView;

        ViewHolder(View itemView) {
            super(itemView);
            studentIdTextView = itemView.findViewById(R.id.student_id_text);
            examNameTextView = itemView.findViewById(R.id.exam_name_text);
            subjectTextView = itemView.findViewById(R.id.subject_text);
            marksTextView = itemView.findViewById(R.id.marks_text);
        }
    }
}