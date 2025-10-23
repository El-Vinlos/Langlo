package com.elvinlos.langlo.ui.exam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.Question;
import com.elvinlos.langlo.R;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.QuestionViewHolder> {

    private final List<Question> questions;
    private final OnAnswerSelectedListener listener;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int position, String answer);
    }

    public ExamAdapter(List<Question> questions, OnAnswerSelectedListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questions.get(position);

        holder.questionNumberTextView.setText("Câu " + (position + 1));
        holder.questionTextView.setText(question.getQuestion());
        holder.optionA.setText(question.getOptionA());
        holder.optionB.setText(question.getOptionB());
        holder.optionC.setText(question.getOptionC());
        holder.optionD.setText(question.getOptionD());

        // Clear previous selection
        holder.optionsRadioGroup.clearCheck();

        // Set listener for answer selection
        holder.optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String answer = "";
            if (checkedId == R.id.optionA) {
                answer = "A";
            } else if (checkedId == R.id.optionB) {
                answer = "B";
            } else if (checkedId == R.id.optionC) {
                answer = "C";
            } else if (checkedId == R.id.optionD) {
                answer = "D";
            }
            listener.onAnswerSelected(position, answer);
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionNumberTextView;
        TextView questionTextView;
        RadioGroup optionsRadioGroup;
        RadioButton optionA, optionB, optionC, optionD;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumberTextView = itemView.findViewById(R.id.questionNumberTextView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            optionsRadioGroup = itemView.findViewById(R.id.optionsRadioGroup);
            optionA = itemView.findViewById(R.id.optionA);
            optionB = itemView.findViewById(R.id.optionB);
            optionC = itemView.findViewById(R.id.optionC);
            optionD = itemView.findViewById(R.id.optionD);
        }
    }
}