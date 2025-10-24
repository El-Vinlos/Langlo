package com.elvinlos.langlo.ui.exam;

import android.content.res.AssetManager;
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

import com.elvinlos.langlo.Deck;
import com.elvinlos.langlo.Question;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.ui.deck.DeckAdapter;
import com.elvinlos.langlo.utils.DrawerHandler;
import com.elvinlos.langlo.utils.Logger;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExamSelectorActivity extends AppCompatActivity {
    private static final String TAG = "EXAM_SELECTOR";
    private final List<Question> questionList = new ArrayList<>();

    FirebaseUser user;
    private DatabaseReference dbRef;
    private DrawerLayout drawerLayout;
    private MaterialToolbar topAppBar;
    private ActionBarDrawerToggle toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_selector);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);

        setSupportActionBar(topAppBar);
        topAppBar.setTitle(getString(R.string.app_name));


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

        RecyclerView recyclerView = findViewById(R.id.recyclerViewDecks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        DeckAdapter deckAdapter = new DeckAdapter(this, questionList);
//        recyclerView.setAdapter(deckAdapter);

        FirebaseDatabase db = FirebaseDatabase.getInstance(
                "https://langlo-7c380-default-rtdb.asia-southeast1.firebasedatabase.app"
        );

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

        loadExams();
    }

    private void loadExams() {
        dbRef.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();
                for (DataSnapshot quizSnap : snapshot.getChildren()) {
                    Question question = quizSnap.getValue(Question.class);
                    if (question != null) {
                        question.setQuestionId(quizSnap.getKey());
                        questionList.add(question);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExamSelectorActivity.this,
                        "Failed to load exams: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
