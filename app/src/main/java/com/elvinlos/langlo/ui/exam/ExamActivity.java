package com.elvinlos.langlo.ui.exam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.elvinlos.langlo.Question;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.User;
import com.elvinlos.langlo.ui.main.MainActivity;
import com.elvinlos.langlo.utils.Navigation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamActivity extends AppCompatActivity {

    private static final String TAG = "ExamActivity";

    private MaterialToolbar toolbar;
    private TextView questionNumberTextView;
    private TextView questionTextView;
    private TextView progressText;
    private TextView scoreText;
    private RadioGroup optionsRadioGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private MaterialButton nextButton;
    private ProgressBar progressBar;
    private LinearProgressIndicator progressIndicator;

    private List<Question> questionList;
    private Map<Integer, String> userAnswers;
    private int currentQuestionIndex = 0;

    private int score = 0;
    private final int TOTAL_QUESTIONS = 10;
    private final int POINTS_PER_QUESTION = 10;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference examsRef;
    private String userId;
    private String examId; // "exam_vocabulary", "exam_grammar", "exam_idioms"
    private String examType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        examsRef = FirebaseDatabase.getInstance().getReference("exams");

        // Get current user
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get exam info from intent
        Intent intent = getIntent();
        examId = intent.getStringExtra("exam_id");
        examType = intent.getStringExtra("exam_type");

        // Fallback nếu không có examId
        if (examId == null) {
            examId = "exam_vocabulary"; // Default
        }

        // Initialize views
        initViews();

        // Initialize user answers map
        userAnswers = new HashMap<>();

        // Load questions from Firebase
        loadQuestionsFromFirebase();

        // Setup listeners
        setupListeners();

        // Setup toolbar back button
        toolbar.setNavigationOnClickListener(v -> showExitDialog());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        questionNumberTextView = findViewById(R.id.questionNumberTextView);
        questionTextView = findViewById(R.id.questionTextView);
        progressText = findViewById(R.id.progressText);
        scoreText = findViewById(R.id.scoreText);
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextButton = findViewById(R.id.nextButton);
        progressBar = findViewById(R.id.progressBar);
        progressIndicator = findViewById(R.id.progressIndicator);
    }

    private void setupListeners() {
        // Enable next button when an answer is selected
        optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            nextButton.setEnabled(true);

            String answer = "";
            if (checkedId == R.id.optionA) answer = "A";
            else if (checkedId == R.id.optionB) answer = "B";
            else if (checkedId == R.id.optionC) answer = "C";
            else if (checkedId == R.id.optionD) answer = "D";

            userAnswers.put(currentQuestionIndex, answer);
        });

        // Next button click
        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questionList.size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            } else {
                // Last question, submit exam
                submitExam();
            }
        });
    }

    private void loadQuestionsFromFirebase() {
        Log.d(TAG, "Loading questions for: " + examId);

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        nextButton.setEnabled(false);

        // Path: exams/exam_vocabulary/questions
        examsRef.child(examId).child("questions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            questionList = new ArrayList<>();

                            for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                                Question question = questionSnapshot.getValue(Question.class);
                                if (question != null) {
                                    questionList.add(question);
                                }
                            }

                            if (!questionList.isEmpty()) {
                                Log.d(TAG, "✅ Loaded " + questionList.size() + " questions");

                                // Shuffle và giới hạn số câu hỏi
                                Collections.shuffle(questionList);
                                if (questionList.size() > TOTAL_QUESTIONS) {
                                    questionList = questionList.subList(0, TOTAL_QUESTIONS);
                                }

                                progressBar.setVisibility(View.GONE);
                                displayQuestion();
                            } else {
                                showError("Không có câu hỏi nào");
                            }
                        } else {
                            showError("Exam không tồn tại: " + examId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Failed to load questions", error.toException());
                        progressBar.setVisibility(View.GONE);
                        showError("Lỗi kết nối: " + error.getMessage());
                    }
                });
    }

    private void displayQuestion() {
        if (questionList == null || questionList.isEmpty()) {
            showError("Không có câu hỏi để hiển thị");
            return;
        }

        Question question = questionList.get(currentQuestionIndex);

        // Update question number and progress
        questionNumberTextView.setText("Câu " + (currentQuestionIndex + 1));
        progressText.setText("Câu " + (currentQuestionIndex + 1) + "/" + questionList.size());

        // Update progress indicator
        int progress = (int) (((currentQuestionIndex + 1) / (float) questionList.size()) * 100);
        progressIndicator.setProgress(progress);

        // Update question and options
        questionTextView.setText(question.getQuestion());
        optionA.setText(question.getOptionA());
        optionB.setText(question.getOptionB());
        optionC.setText(question.getOptionC());
        optionD.setText(question.getOptionD());

        // Clear selection
        optionsRadioGroup.clearCheck();

        // Restore previous answer if exists
        if (userAnswers.containsKey(currentQuestionIndex)) {
            String previousAnswer = userAnswers.get(currentQuestionIndex);
            if ("A".equals(previousAnswer)) optionA.setChecked(true);
            else if ("B".equals(previousAnswer)) optionB.setChecked(true);
            else if ("C".equals(previousAnswer)) optionC.setChecked(true);
            else if ("D".equals(previousAnswer)) optionD.setChecked(true);
            nextButton.setEnabled(true);
        } else {
            nextButton.setEnabled(false);
        }

        // Change button text for last question
        if (currentQuestionIndex == questionList.size() - 1) {
            nextButton.setText("Nộp bài");
            nextButton.setIcon(null);
        } else {
            nextButton.setText("Câu tiếp theo");
        }

        // Log for debugging
        Log.d(TAG, "Displaying Question " + (currentQuestionIndex + 1));
        Log.d(TAG, "Question: " + question.getQuestion());
        Log.d(TAG, "Correct Answer: " + question.getCorrectAnswer());
    }

    private void submitExam() {
        // Check if all questions are answered
        if (userAnswers.size() < questionList.size()) {
            Toast.makeText(this, "Vui lòng trả lời tất cả các câu hỏi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate score
        score = 0;
        for (int i = 0; i < questionList.size(); i++) {
            Question question = questionList.get(i);
            String userAnswer = userAnswers.get(i);

            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswer())) {
                score += POINTS_PER_QUESTION;
            }
        }

        Log.d(TAG, "Final Score: " + score + "/" + (questionList.size() * POINTS_PER_QUESTION));

        finishExam();
    }

    private void finishExam() {
        progressBar.setVisibility(View.VISIBLE);
        nextButton.setEnabled(false);

        databaseReference.child("users").child(userId).get().addOnSuccessListener(dataSnapshot -> {
            User user = dataSnapshot.getValue(User.class);
            if (user != null) {
                int newTotalScore = user.getTotalScore() + score;
                int newGamesPlayed = user.getGamesPlayed() + 1;

                // Update user stats
                databaseReference.child("users").child(userId).child("totalScore").setValue(newTotalScore);
                databaseReference.child("users").child(userId).child("gamesPlayed").setValue(newGamesPlayed);

                // Update exam-specific high score
                if (examId != null) {
                    databaseReference.child("users").child(userId)
                            .child("examScores").child(examId)
                            .get().addOnSuccessListener(examScoreSnapshot -> {
                                Long currentHighScore = examScoreSnapshot.getValue(Long.class);
                                if (currentHighScore == null || score > currentHighScore) {
                                    databaseReference.child("users").child(userId)
                                            .child("examScores").child(examId).setValue((long) score);
                                }
                            });
                }

                // Update leaderboard
                databaseReference.child("leaderboard").child(userId).child("name").setValue(user.getName());
                databaseReference.child("leaderboard").child(userId).child("totalScore").setValue(newTotalScore);
                databaseReference.child("leaderboard").child(userId).child("gamesPlayed").setValue(newGamesPlayed);

                progressBar.setVisibility(View.GONE);
                showResultDialog(newTotalScore, newGamesPlayed);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            nextButton.setEnabled(true);
            Toast.makeText(this, "Lỗi khi lưu điểm!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error saving score", e);
        });
    }

    private void showResultDialog(int totalScore, int gamesPlayed) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialAlertDialog);
        builder.setTitle("Hoàn thành Bài kiểm tra!");
        builder.setMessage("Điểm lần này: " + score + " điểm\n" +
                "Tổng điểm của bạn: " + totalScore + " điểm\n" +
                "Tổng số lần làm: " + gamesPlayed + " lần");
        builder.setPositiveButton("Về trang chủ", (dialog, which) -> {
            Intent intent = new Intent(ExamActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Làm lại", (dialog, which) -> {
            Intent intent = new Intent(ExamActivity.this, ExamActivity.class);
            intent.putExtra("exam_id", examId);
            intent.putExtra("exam_type", examType);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showExitDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialAlertDialog);
        builder.setTitle("Thoát bài kiểm tra?");
        builder.setMessage("Bạn có chắc muốn thoát? Điểm số sẽ không được lưu!");
        builder.setPositiveButton("Thoát", (dialog, which) -> Navigation.navigateToActivity(this, ExamSelectorActivity.class));
        builder.setNegativeButton("Tiếp tục", null);
        builder.show();
    }

    private void showError(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);

        // Hiển thị dialog lỗi và quay về MainActivity
        new MaterialAlertDialogBuilder(this, R.style.CustomMaterialAlertDialog)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    Navigation.navigateToActivity(this, MainActivity.class);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

//    @Override
//    public void onBackPressed() {
//        showExitDialog();
//    }
}