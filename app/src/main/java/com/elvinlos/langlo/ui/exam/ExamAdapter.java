package com.elvinlos.langlo.ui.exam;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.Exam;
import com.elvinlos.langlo.R;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private final List<Exam> examList;
    private final Context context;

    public ExamAdapter(Context context, List<Exam> examList) {
        this.examList = examList;
        this.context = context;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.title.setText(exam.getTitle());
        holder.questionAmount.setText(exam.getQuestionAmount());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExamActivity.class);

            // ✅ Truyền examId (cần lưu trong Exam class)
            // Tạm thời dùng title để convert ngược
            String examId = exam.getTitle().toLowerCase().replace(" ", "_");
            if (!examId.startsWith("exam_")) {
                examId = "exam_" + examId;
            }

            intent.putExtra("examId", examId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView title, questionAmount;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textExamTitle);
            questionAmount = itemView.findViewById(R.id.textExamQuestionAmount);
        }
    }
}