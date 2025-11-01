package com.school.edutrack.ui.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.Student;
import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList;
    private OnStudentActionListener actionListener;

    public interface OnStudentActionListener {
        void onUpdate(Student student);
        void onDelete(Student student);
    }

    public StudentAdapter(OnStudentActionListener listener) {
        this.studentList = new ArrayList<>();
        this.actionListener = listener;
    }

    public void setStudentList(List<Student> students) {
        this.studentList = students;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.nameText.setText(student.getName() != null ? student.getName() : "N/A");
        holder.classText.setText(student.getClass_name() != null && student.getSection() != null
                ? "Class: " + student.getClass_name() + "-" + student.getSection()
                : "Class: N/A");
        holder.admissionDateText.setText(student.getAdmission_date() != null
                ? "Admission Date: " + student.getAdmission_date()
                : "Admission Date: N/A");

        holder.editButton.setOnClickListener(v -> actionListener.onUpdate(student));
        holder.deleteButton.setOnClickListener(v -> actionListener.onDelete(student));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, classText, admissionDateText;
        Button editButton, deleteButton;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.student_name);
            classText = itemView.findViewById(R.id.student_class);
            admissionDateText = itemView.findViewById(R.id.student_admission_date);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}