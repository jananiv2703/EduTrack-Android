package com.school.edutrack.ui.teacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textview.MaterialTextView;
import com.school.edutrack.R;
import com.school.edutrack.model.Attendance;
import com.school.edutrack.model.Student;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.StudentViewHolder> {

    private List<Student> students;
    private Context context;
    private List<String> statusOptions;
    private List<String> selectedStatuses; // To store the selected status for each student

    public StudentAttendanceAdapter(Context context, List<Student> students) {
        this.context = context;
        this.students = students;
        this.statusOptions = Arrays.asList("Select Status", "Present", "Absent", "Leave", "On Duty");
        this.selectedStatuses = new ArrayList<>();
        // Initialize the status list with "Select Status" for each student
        for (int i = 0; i < students.size(); i++) {
            selectedStatuses.add("Select Status");
        }
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_attendance, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.studentName.setText(student.getName());
        holder.studentId.setText("ID: " + student.getStudent_id());

        // Setup status dropdown
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusDropdown.setAdapter(statusAdapter);

        // Preselect the status if it exists (from a separate fetch or previous selection)
        String currentStatus = student.getStatus();
        if (currentStatus != null && !currentStatus.isEmpty()) {
            String formattedStatus = currentStatus.toLowerCase();
            if (formattedStatus.equals("od")) {
                formattedStatus = "On Duty";
            } else {
                formattedStatus = formattedStatus.substring(0, 1).toUpperCase() + formattedStatus.substring(1);
            }
            int statusPosition = statusOptions.indexOf(formattedStatus);
            if (statusPosition != -1) {
                holder.statusDropdown.setSelection(statusPosition);
                selectedStatuses.set(position, formattedStatus); // Update the stored status
            }
        } else {
            // Restore the previously selected status (if any)
            String savedStatus = selectedStatuses.get(position);
            int savedPosition = statusOptions.indexOf(savedStatus);
            holder.statusDropdown.setSelection(savedPosition);
        }

        // Listen for status changes and update the selectedStatuses list
        holder.statusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedStatus = parent.getItemAtPosition(pos).toString();
                selectedStatuses.set(position, selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public List<Attendance> getAttendanceRecords(String date, String teacherId) {
        List<Attendance> attendanceRecords = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            String status = selectedStatuses.get(i);

            if (status.equals("Select Status")) continue; // Skip if status not selected

            String formattedStatus = status.toLowerCase().replace("on duty", "od");
            Attendance attendance = new Attendance(
                    student.getStudent_id(),
                    date,
                    formattedStatus,
                    teacherId
            );
            attendanceRecords.add(attendance);
        }
        return attendanceRecords;
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView studentName, studentId;
        Spinner statusDropdown;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.student_name);
            studentId = itemView.findViewById(R.id.student_id);
            statusDropdown = itemView.findViewById(R.id.attendance_status_dropdown);
        }
    }
}