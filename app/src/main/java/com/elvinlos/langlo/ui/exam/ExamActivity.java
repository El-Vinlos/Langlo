package com.elvinlos.langlo.ui.exam;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamActivity extends AppCompatActivity {

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
    private String userId;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get quiz ID from intent
        Intent intent = getIntent();
        quizId = intent.getStringExtra("quiz_id");

        // Initialize views
        initViews();

        // Initialize user answers map
        userAnswers = new HashMap<>();

        // Load questions from JSON
        loadQuestionsFromJson();

        // Display first question
        displayQuestion();

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

    private void displayQuestion() {
        if (questionList == null || questionList.isEmpty()) return;

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
    }

    private void loadQuestionsFromJson() {
        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            JSONArray questionsArray = jsonObject.getJSONArray("questions");

            questionList = new ArrayList<>();

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObj = questionsArray.getJSONObject(i);

                Question question = new Question(
                        questionObj.getString("questionId"),
                        questionObj.getString("question"),
                        questionObj.getString("optionA"),
                        questionObj.getString("optionB"),
                        questionObj.getString("optionC"),
                        questionObj.getString("optionD"),
                        questionObj.getString("correctAnswer")
                );

                questionList.add(question);
            }

            Collections.shuffle(questionList);
            questionList = questionList.subList(0, Math.min(TOTAL_QUESTIONS, questionList.size()));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tải câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
        }
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

                databaseReference.child("users").child(userId).child("totalScore").setValue(newTotalScore);
                databaseReference.child("users").child(userId).child("gamesPlayed").setValue(newGamesPlayed);

                if (quizId != null) {
                    databaseReference.child("users").child(userId)
                            .child("quizScores").child(quizId)
                            .get().addOnSuccessListener(quizScoreSnapshot -> {
                                Long currentHighScore = quizScoreSnapshot.getValue(Long.class);
                                if (currentHighScore == null || score > currentHighScore) {
                                    databaseReference.child("users").child(userId)
                                            .child("quizScores").child(quizId).setValue((long) score);
                                }
                            });
                }

                databaseReference.child("leaderboard").child(userId).child("username").setValue(user.getUsername());
                databaseReference.child("leaderboard").child(userId).child("totalScore").setValue(newTotalScore);
                databaseReference.child("leaderboard").child(userId).child("gamesPlayed").setValue(newGamesPlayed);

                progressBar.setVisibility(View.GONE);
                showResultDialog(newTotalScore, newGamesPlayed);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            nextButton.setEnabled(true);
            Toast.makeText(this, "Lỗi khi lưu điểm!", Toast.LENGTH_SHORT).show();
        });
    }

    private void showResultDialog(int totalScore, int gamesPlayed) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
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
            intent.putExtra("quiz_id", quizId);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showExitDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Thoát bài kiểm tra?");
        builder.setMessage("Bạn có chắc muốn thoát? Điểm số sẽ không được lưu!");
        builder.setPositiveButton("Thoát", (dialog, which) -> finish());
        builder.setNegativeButton("Tiếp tục", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }
}