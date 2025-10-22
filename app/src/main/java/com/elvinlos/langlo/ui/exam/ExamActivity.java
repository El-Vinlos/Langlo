package com.elvinlos.langlo.ui.exam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.R;
import com.elvinlos.langlo.ui.exam.ExamAdapter;
import com.elvinlos.langlo.Question;
import com.elvinlos.langlo.User;
import com.elvinlos.langlo.ui.main.MainActivity;
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

public class ExamActivity extends AppCompatActivity implements ExamAdapter.OnAnswerSelectedListener {
    private static final String TAG = "ExamActivity";
    private RecyclerView questionsRecyclerView;
    private Button submitButton;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private ExamAdapter adapter;
    private List<Question> questionList;
    private Map<Integer, String> userAnswers; // Store user's answers

    private int score = 0;
    private final int TOTAL_QUESTIONS = 10;
    private final int POINTS_PER_QUESTION = 10;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userId;

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

        // Initialize views
        initViews();

        // Initialize user answers map
        userAnswers = new HashMap<>();

        // Load questions from JSON
        loadQuestionsFromJson();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup submit button
        submitButton.setOnClickListener(v -> submitExam());

        // Setup toolbar back button
        toolbar.setNavigationOnClickListener(v -> showExitDialog());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new ExamAdapter(questionList, this);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        questionsRecyclerView.setAdapter(adapter);

        // Add snap helper for page-like scrolling
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(questionsRecyclerView);
    }

    private void loadQuestionsFromJson() {
        try {
            // Read JSON file from assets
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            // Parse JSON
            JSONObject jsonObject = new JSONObject(json);
            JSONArray questionsArray = jsonObject.getJSONArray("questions");

            questionList = new ArrayList<>();

            // Convert JSON array to Question objects
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

            // Shuffle questions and take only TOTAL_QUESTIONS
            Collections.shuffle(questionList);
            questionList = questionList.subList(0, Math.min(TOTAL_QUESTIONS, questionList.size()));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tải câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onAnswerSelected(int position, String answer) {
        // Store user's answer
        userAnswers.put(position, answer);
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

        // Save score to Firebase
        finishExam();
    }

    private void finishExam() {
        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        // Update user score in Firebase
        databaseReference.child("users").child(userId).get().addOnSuccessListener(dataSnapshot -> {
            User user = dataSnapshot.getValue(User.class);
            if (user != null) {
                // Update user stats
                int newTotalScore = user.getTotalScore() + score;
                int newGamesPlayed = user.getGamesPlayed() + 1;

                // Update in Firebase
                databaseReference.child("users").child(userId).child("totalScore").setValue(newTotalScore);
                databaseReference.child("users").child(userId).child("gamesPlayed").setValue(newGamesPlayed);

                // Update leaderboard
                databaseReference.child("leaderboard").child(userId).child("username").setValue(user.getUsername());
                databaseReference.child("leaderboard").child(userId).child("totalScore").setValue(newTotalScore);
                databaseReference.child("leaderboard").child(userId).child("gamesPlayed").setValue(newGamesPlayed);

                progressBar.setVisibility(View.GONE);
                showResultDialog(newTotalScore, newGamesPlayed);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi khi lưu điểm!", Toast.LENGTH_SHORT).show();
        });
    }

    private void showResultDialog(int totalScore, int gamesPlayed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thoát bài kiểm tra?");
        builder.setMessage("Bạn có chắc muốn thoát? Điểm số sẽ không được lưu!");
        builder.setPositiveButton("Thoát", (dialog, which) -> finish());
        builder.setNegativeButton("Tiếp tục", null);
        builder.show();
    }
}