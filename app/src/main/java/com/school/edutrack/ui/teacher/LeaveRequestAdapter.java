package com.school.edutrack.ui.teacher;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.LeaveRequest;
import java.util.List;
import pl.droidsonroids.gif.GifImageView;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.LeaveRequestViewHolder> {

    private List<LeaveRequest> leaveRequests;
    private Context context;
    private OnStatusUpdateListener listener;

    public interface OnStatusUpdateListener {
        void onStatusUpdate(int leaveRequestId, String newStatus);
    }

    public LeaveRequestAdapter(Context context, List<LeaveRequest> leaveRequests, OnStatusUpdateListener listener) {
        this.context = context;
        this.leaveRequests = leaveRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LeaveRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leave_request, parent, false);
        return new LeaveRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveRequestViewHolder holder, int position) {
        LeaveRequest request = leaveRequests.get(position);

        holder.studentName.setText("Student Name: " + (request.getStudentName() != null ? request.getStudentName() : "Not specified"));
        holder.studentId.setText("Student ID: " + (request.getStudentId() != null ? request.getStudentId() : "Not specified"));
        holder.classSection.setText("Class & Section: " +
                (request.getClassName() != null ? request.getClassName() : "N/A") + " " +
                (request.getSection() != null ? request.getSection() : ""));
        holder.leaveDates.setText("Leave Dates: " + request.getLeaveFrom() + " to " + request.getLeaveTo());
        holder.reason.setText("Reason: " + request.getReason());
        holder.status.setText("Status: " + request.getStatus());

        // Set status icon and color
        String status = request.getStatus() != null ? request.getStatus() : "Unknown";
        switch (status.toLowerCase()) {
            case "approved":
                holder.statusIcon.setImageResource(R.drawable.ic_success);
                holder.status.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "rejected":
                holder.statusIcon.setImageResource(R.drawable.ic_rejected);
                holder.status.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            case "pending":
                holder.statusIcon.setImageResource(R.drawable.ic_waiting);
                holder.status.setTextColor(Color.parseColor("#FFC107")); // Yellow
                break;
            default:
                holder.statusIcon.setImageResource(0); // Clear icon if status is unknown
                holder.status.setTextColor(Color.parseColor("#757575")); // Gray
                break;
        }

        // Configure the status dropdown
        if (request.getStatus().equals("Pending")) {
            holder.statusDropdownLayout.setVisibility(View.VISIBLE);
            // Set the default selection to "Pending"
            holder.statusDropdown.setSelection(0); // "Pending" is the first option

            // Handle status change
            holder.statusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String newStatus = parent.getItemAtPosition(position).toString();
                    if (!newStatus.equals("Pending")) { // Only trigger update if the status changes to Approved or Rejected
                        listener.onStatusUpdate(request.getId(), newStatus);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        } else {
            holder.statusDropdownLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return leaveRequests.size();
    }

    public void updateLeaveRequests(List<LeaveRequest> newLeaveRequests) {
        this.leaveRequests = newLeaveRequests;
        notifyDataSetChanged();
    }

    public static class LeaveRequestViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, studentId, classSection, leaveDates, reason, status;
        GifImageView statusIcon;
        com.google.android.material.textfield.TextInputLayout statusDropdownLayout;
        Spinner statusDropdown;

        public LeaveRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.student_name);
            studentId = itemView.findViewById(R.id.student_id);
            classSection = itemView.findViewById(R.id.class_section);
            leaveDates = itemView.findViewById(R.id.leave_dates);
            reason = itemView.findViewById(R.id.reason);
            status = itemView.findViewById(R.id.status);
            statusIcon = itemView.findViewById(R.id.status_icon);
            statusDropdownLayout = itemView.findViewById(R.id.status_dropdown_layout);
            statusDropdown = itemView.findViewById(R.id.status_dropdown);
        }
    }
}