package com.elvinlos.langlo.ui.exam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.R;
import com.elvinlos.langlo.Question;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.QuestionViewHolder> {

    private List<Question> questionList;
    private OnAnswerSelectedListener listener;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int position, String answer);
    }

    public ExamAdapter(List<Question> questionList, OnAnswerSelectedListener listener) {
        this.questionList = questionList;
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
        Question question = questionList.get(position);
        holder.bind(question, position);
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private TextView questionNumberTextView;
        private TextView questionTextView;
        private RadioGroup optionsRadioGroup;
        private RadioButton optionA, optionB, optionC, optionD;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumberTextView = itemView.findViewById(R.id.questionNumberTextView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            optionsRadioGroup = itemView.findViewById(R.id.optionsRadioGroup);
            optionA = itemView.findViewById(R.id.optionA);
            optionB = itemView.findViewById(R.id.optionB);
            optionC = itemView.findViewById(R.id.optionC);
            optionD = itemView.findViewById(R.id.optionD);
        }

        public void bind(Question question, int position) {
            // Set question number and text
            questionNumberTextView.setText("Câu " + (position + 1));
            questionTextView.setText(question.getQuestion());

            // Set options
            optionA.setText("A. " + question.getOptionA());
            optionB.setText("B. " + question.getOptionB());
            optionC.setText("C. " + question.getOptionC());
            optionD.setText("D. " + question.getOptionD());

            // Clear previous selection
            optionsRadioGroup.clearCheck();

            // Setup listeners
            setupRadioButtonListeners(position);
        }

        private void setupRadioButtonListeners(int position) {
            View.OnClickListener clickListener = v -> {
                int id = v.getId();
                optionsRadioGroup.check(id);

                String selectedAnswer = "";
                if (id == R.id.optionA) selectedAnswer = "A";
                else if (id == R.id.optionB) selectedAnswer = "B";
                else if (id == R.id.optionC) selectedAnswer = "C";
                else if (id == R.id.optionD) selectedAnswer = "D";

                if (listener != null) {
                    listener.onAnswerSelected(position, selectedAnswer);
                }
            };

            optionA.setOnClickListener(clickListener);
            optionB.setOnClickListener(clickListener);
            optionC.setOnClickListener(clickListener);
            optionD.setOnClickListener(clickListener);
        }
    }
}