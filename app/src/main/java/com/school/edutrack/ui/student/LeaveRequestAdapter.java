package com.school.edutrack.ui.student;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentLeaveRequest;
import java.util.List;
import pl.droidsonroids.gif.GifImageView;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.LeaveRequestViewHolder> {

    private static final String TAG = "LeaveRequestAdapter";
    private List<StudentLeaveRequest> leaveRequests;
    private Context context;

    public LeaveRequestAdapter(List<StudentLeaveRequest> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }

    @NonNull
    @Override
    public LeaveRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        Log.d(TAG, "Inflating layout R.layout.student_item_leave_request (ID: " + R.layout.student_item_leave_request + ") at 03:56 PM IST, Friday, June 13, 2025");
        View view = LayoutInflater.from(context).inflate(R.layout.student_item_leave_request, parent, false);
        return new LeaveRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveRequestViewHolder holder, int position) {
        StudentLeaveRequest leaveRequest = leaveRequests.get(position);
        Log.d(TAG, "Binding leave request at position " + position + " at 03:56 PM IST, Friday, June 13, 2025");

        // Handle null values for dates
        String leaveFrom = leaveRequest.getLeave_from() != null ? leaveRequest.getLeave_from() : "Not specified";
        String leaveTo = leaveRequest.getLeave_to() != null ? leaveRequest.getLeave_to() : "Not specified";
        holder.leaveDatesText.setText("From: " + leaveFrom + " To: " + leaveTo);

        // Handle null value for reason
        String reason = leaveRequest.getReason() != null ? leaveRequest.getReason() : "Not specified";
        holder.reasonText.setText("Reason: " + reason);

        // Handle status with icon and color
        String status = leaveRequest.getStatus() != null ? leaveRequest.getStatus() : "Unknown";
        holder.statusText.setText("Status: " + status);
        switch (status.toLowerCase()) {
            case "approved":
                holder.statusIcon.setImageResource(R.drawable.ic_success);
                holder.statusText.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "rejected":
                holder.statusIcon.setImageResource(R.drawable.ic_rejected);
                holder.statusText.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            case "pending":
                holder.statusIcon.setImageResource(R.drawable.ic_waiting);
                holder.statusText.setTextColor(Color.parseColor("#FFC107")); // Yellow
                break;
            default:
                holder.statusIcon.setImageResource(0); // Clear icon if status is unknown
                holder.statusText.setTextColor(Color.parseColor("#757575")); // Gray
                break;
        }

        // Handle null value for proof
        holder.proofText.setText(leaveRequest.getProof() != null ? "Proof: " + leaveRequest.getProof() : "Proof: Not provided");
    }

    @Override
    public int getItemCount() {
        return leaveRequests.size();
    }

    public static class LeaveRequestViewHolder extends RecyclerView.ViewHolder {
        TextView leaveDatesText, reasonText, statusText, proofText;
        GifImageView statusIcon;

        public LeaveRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            leaveDatesText = itemView.findViewById(R.id.leave_dates_text);
            reasonText = itemView.findViewById(R.id.reason_text);
            statusText = itemView.findViewById(R.id.status_text);
            proofText = itemView.findViewById(R.id.proof_text);
            statusIcon = itemView.findViewById(R.id.status_icon);

            // Debug logging to check if views are found
            if (leaveDatesText == null) {
                Log.e(TAG, "leaveDatesText is null at 03:56 PM IST, Friday, June 13, 2025");
            }
            if (reasonText == null) {
                Log.e(TAG, "reasonText is null at 03:56 PM IST, Friday, June 13, 2025");
            }
            if (statusText == null) {
                Log.e(TAG, "statusText is null at 03:56 PM IST, Friday, June 13, 2025");
            }
            if (proofText == null) {
                Log.e(TAG, "proofText is null at 03:56 PM IST, Friday, June 13, 2025");
            }
            if (statusIcon == null) {
                Log.e(TAG, "statusIcon is null at 03:56 PM IST, Friday, June 13, 2025");
            }
        }
    }
}