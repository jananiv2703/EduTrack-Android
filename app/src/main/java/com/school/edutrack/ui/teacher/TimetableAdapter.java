package com.school.edutrack.ui.teacher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.Timetable;
import java.util.ArrayList;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder> {

    private List<Timetable> timetableEntries = new ArrayList<>();

    public void setTimetableEntries(List<Timetable> entries) {
        this.timetableEntries = entries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimetableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable_entry, parent, false);
        return new TimetableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableViewHolder holder, int position) {
        Timetable entry = timetableEntries.get(position);
        holder.periodText.setText(entry.getPeriodNo());
        holder.subjectText.setText(entry.getSubject());
        holder.classText.setText(entry.getClassName());
        holder.sectionText.setText(entry.getSection());

        // Add a subtle animation
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 50)
                .start();
    }

    @Override
    public int getItemCount() {
        return timetableEntries.size();
    }

    static class TimetableViewHolder extends RecyclerView.ViewHolder {
        TextView periodText, subjectText, classText, sectionText;

        TimetableViewHolder(@NonNull View itemView) {
            super(itemView);
            periodText = itemView.findViewById(R.id.period_text);
            subjectText = itemView.findViewById(R.id.subject_text);
            classText = itemView.findViewById(R.id.class_text);
            sectionText = itemView.findViewById(R.id.section_text);
        }
    }
}