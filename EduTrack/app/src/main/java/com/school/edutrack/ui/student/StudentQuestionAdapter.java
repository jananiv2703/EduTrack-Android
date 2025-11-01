package com.school.edutrack.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.school.edutrack.R;
import com.school.edutrack.model.StudentQuiz;
import java.util.List;

public class StudentQuestionAdapter extends RecyclerView.Adapter<StudentQuestionAdapter.QuestionViewHolder> {

    private List<StudentQuiz.Question> questions;

    public StudentQuestionAdapter(List<StudentQuiz.Question> questions) {
        this.questions = questions;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_quiz_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        StudentQuiz.Question question = questions.get(position);
        holder.questionText.setText(question.getQuestionText());
        holder.optionA.setText(question.getOptionA());
        holder.optionB.setText(question.getOptionB());
        holder.optionC.setText(question.getOptionC());
        holder.optionD.setText(question.getOptionD());

        // Set previously selected option, if any
        if (question.getSelectedOption() != -1) {
            holder.optionsRadioGroup.check(getRadioButtonId(question.getSelectedOption(), holder));
        }

        // Handle option selection
        holder.optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedOption = -1;
            if (checkedId == holder.optionA.getId()) selectedOption = 0;
            else if (checkedId == holder.optionB.getId()) selectedOption = 1;
            else if (checkedId == holder.optionC.getId()) selectedOption = 2;
            else if (checkedId == holder.optionD.getId()) selectedOption = 3;
            question.setSelectedOption(selectedOption);
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    // Add getQuestions() method to return the list of questions with user selections
    public List<StudentQuiz.Question> getQuestions() {
        return questions;
    }

    private int getRadioButtonId(int selectedOption, QuestionViewHolder holder) {
        switch (selectedOption) {
            case 0: return holder.optionA.getId();
            case 1: return holder.optionB.getId();
            case 2: return holder.optionC.getId();
            case 3: return holder.optionD.getId();
            default: return -1;
        }
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        RadioGroup optionsRadioGroup;
        RadioButton optionA, optionB, optionC, optionD;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.student_question_text);
            optionsRadioGroup = itemView.findViewById(R.id.student_options_radio_group);
            optionA = itemView.findViewById(R.id.student_option_a);
            optionB = itemView.findViewById(R.id.student_option_b);
            optionC = itemView.findViewById(R.id.student_option_c);
            optionD = itemView.findViewById(R.id.student_option_d);
        }
    }
}