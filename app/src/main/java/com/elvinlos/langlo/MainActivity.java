package com.elvinlos.langlo;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.utils.DrawerHandler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<Deck> deckList = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private MaterialToolbar topAppBar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        loadDecksFromAssets();

        DeckAdapter deckAdapter = new DeckAdapter(this, deckList);
        recyclerView.setAdapter(deckAdapter);

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
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    private void loadDecksFromAssets() {
        try {
            AssetManager assetManager = getAssets();
            String[] deckFolders = assetManager.list("deck");
            if (deckFolders == null) {
                Logger.e("DeckLoader", "No 'deck' folder found in assets!");
                return;
            }
            for (String folder : deckFolders) {
                String path = "deck/" + folder + "/deck_property.json";
                try (InputStream is = assetManager.open(path);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder jsonBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) jsonBuilder.append(line);
                    JSONObject json = new JSONObject(jsonBuilder.toString());
                    String title = json.optString("title", folder);
                    String description = json.optString("description", "");
                    int cardCount = json.optInt("cardCount", 0);
                    deckList.add(new Deck(title, description, cardCount, folder));
                } catch (Exception e) {
                    Logger.e("DeckLoader", "Error reading deck_property.json in folder: " + folder, e);
                }
            }
        } catch (Exception e) {
            Logger.e("DeckLoader", "Error listing deck folders", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}
