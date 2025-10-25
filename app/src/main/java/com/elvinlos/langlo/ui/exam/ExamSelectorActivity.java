package com.elvinlos.langlo.ui.exam;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.Exam;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.utils.DrawerHandler;
import com.elvinlos.langlo.utils.Logger;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExamSelectorActivity extends AppCompatActivity {
    private static final String TAG = "EXAM_SELECTOR";

    private final List<Exam> examList = new ArrayList<>();
    private DatabaseReference examsRef;
    private ExamAdapter examAdapter; // ✅ Khai báo ở đây
    private DrawerLayout drawerLayout;
    private MaterialToolbar topAppBar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_selector);

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);

        setSupportActionBar(topAppBar);
        topAppBar.setTitle("Trắc nghiệm");

        // Initialize Firebase
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                "https://langlo-7c380-default-rtdb.asia-southeast1.firebasedatabase.app"
        );
        examsRef = db.getReference("exams");

        // Setup Drawer
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            DrawerHandler.handleDrawerItem(this, drawerLayout, topAppBar, item);
            return true;
        });

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewExams);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Khởi tạo adapter trước
        examAdapter = new ExamAdapter(this, examList);
        recyclerView.setAdapter(examAdapter);

        // Setup back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    finish();
                    setEnabled(true);
                }
            }
        });

        // Load data
        loadExamsFromFirebase();
    }

    private void loadExamsFromFirebase() {
        Logger.d(TAG, "Loading exams from Firebase...");

        examsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                examList.clear();

                if (snapshot.exists()) {
                    Logger.d(TAG, "Found exams node, processing...");

                    for (DataSnapshot examSnapshot : snapshot.getChildren()) {
                        String examId = examSnapshot.getKey();
                        Logger.d(TAG, "Processing exam: " + examId);

                        // Đếm số câu hỏi
                        DataSnapshot questionsSnapshot = examSnapshot.child("questions");
                        int questionCount = (int) questionsSnapshot.getChildrenCount();

                        // Lấy title
                        String title = examSnapshot.child("title").getValue(String.class);
                        if (title == null || title.isEmpty()) {
                            title = formatExamTitle(examId);
                        }

                        // Tạo Exam object
                        Exam exam = new Exam(title, questionCount + " câu hỏi");
                        examList.add(exam);

                        Logger.d(TAG, "✅ Added exam: " + title + " (" + questionCount + " questions)");
                    }

                    // ✅ Cập nhật adapter
                    examAdapter.notifyDataSetChanged();
                    Logger.d(TAG, "Total exams loaded: " + examList.size());
                } else {
                    Logger.w(TAG, "No exams found in database at path: exams/");
                    Toast.makeText(ExamSelectorActivity.this,
                            "Chưa có đề thi nào",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Logger.e(TAG, "Failed to load exams: " + error.getMessage());
                Toast.makeText(ExamSelectorActivity.this,
                        "Lỗi: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatExamTitle(String examId) {
        if (examId == null || examId.isEmpty()) return "Unknown";

        String title = examId.replace("exam_", "").replace("_", " ");
        if (title.length() > 0) {
            title = title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        return title;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}