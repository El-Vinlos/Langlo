package com.elvinlos.langlo.ui.speech;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.utils.DrawerHandler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.elvinlos.langlo.R;
import java.util.ArrayList;
import java.util.List;

public class SpeakActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speak_activity);

        initViews();
//        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // If you want to call the default behavior, you need to disable this callback and re-dispatch the event.
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.topAppBar);
        recyclerView = findViewById(R.id.recyclerViewSpeech);
    }

//    private void setupToolbar() {
//        setSupportActionBar(toolbar);
//
//        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
//
//        toolbar.setOnMenuItemClickListener(menuItem -> {
//            if (menuItem.getItemId() == R.id.TEST) {
//                // Handle settings
//                return true;
//            }
//            return false;
//        });
//    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            DrawerHandler.handleDrawerItem(this, drawerLayout, toolbar, item);
            return true;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupRecyclerView() {
        List<SpeechItem> speechItems = new ArrayList<>();
        speechItems.add(new SpeechItem("Hello", 0));
        speechItems.add(new SpeechItem("Pronunciation", 0));
        speechItems.add(new SpeechItem("Beautiful", 0));
        speechItems.add(new SpeechItem("Wednesday", 0));
        speechItems.add(new SpeechItem("Restaurant", 0));
        speechItems.add(new SpeechItem("Library", 0));
        speechItems.add(new SpeechItem("Chocolate", 0));
        speechItems.add(new SpeechItem("Temperature", 0));
        speechItems.add(new SpeechItem("Comfortable", 0));
        speechItems.add(new SpeechItem("Vegetable", 0));
        speechItems.add(new SpeechItem("Necessary", 0));
        speechItems.add(new SpeechItem("Particularly", 0));

        SpeechAdapter speechAdapter = new SpeechAdapter(speechItems, item -> {
            Intent intent = new Intent(SpeakActivity.this, PronunciationTestActivity.class);
            intent.putExtra("WORD_TO_TEST", item.getTitle());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(speechAdapter);
    }

}
