package com.elvinlos.langlo;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DeckActivity extends AppCompatActivity {

    private static final String TAG = "DeckActivity";

    private DrawerLayout drawerLayoutDeck;
    private NavigationView navigationViewDeck;
    private MaterialToolbar topAppBarDeck;
    private ActionBarDrawerToggle toggle;
    private RecyclerView recyclerViewCards;
    private CardAdapter cardAdapter;

    private final List<Card> cardList = new ArrayList<>();
    private final List<String> deckFolders = new ArrayList<>();
    private final List<String> deckTitles = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        drawerLayoutDeck = findViewById(R.id.drawerLayoutDeck);
        navigationViewDeck = findViewById(R.id.navigationViewDeck);
        topAppBarDeck = findViewById(R.id.topAppBarDeck);
        recyclerViewCards = findViewById(R.id.recyclerViewCards);

        setSupportActionBar(topAppBarDeck);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayoutDeck,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayoutDeck.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navigationViewDeck.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (id == R.id.nav_settings) {
                topAppBarDeck.setTitle("Settings");
                topAppBarDeck.getMenu().clear();
            }
            drawerLayoutDeck.closeDrawer(GravityCompat.START);
            return true;
        });

        recyclerViewCards.setLayoutManager(new LinearLayoutManager(this));
        cardAdapter = new CardAdapter(this, cardList);
        recyclerViewCards.setAdapter(cardAdapter);

        loadDeckFoldersAndTitles();

        // Load first deck automatically if available
        if (!deckFolders.isEmpty()) {
            Log.d(TAG, "Loading first deck: " + deckFolders.get(0));
            loadCardsFromDeck(deckFolders.get(0));
        }
    }

    private void loadDeckFoldersAndTitles() {
        try {
            AssetManager assetManager = getAssets();
            String[] folders = assetManager.list("deck");
            if (folders != null) {
                for (String folder : folders) {
                    try {
                        String path = "deck/" + folder + "/deck_property.json";
                        InputStream is = assetManager.open(path);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder jsonBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) jsonBuilder.append(line);
                        JSONObject json = new JSONObject(jsonBuilder.toString());
                        String title = json.optString("title", folder);

                        deckFolders.add(folder);
                        deckTitles.add(title);

                        Log.d(TAG, "Loaded deck: " + folder + " with title: " + title);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading deck_property.json in folder: " + folder, e);
                        deckFolders.add(folder);
                        deckTitles.add(folder);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading deck folders", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_deck, menu);

        MenuItem spinnerItem = menu.findItem(R.id.action_deck_spinner);
        Spinner spinner = (Spinner) spinnerItem.getActionView();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, deckTitles);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String folderName = deckFolders.get(position);
                Log.d(TAG, "Spinner selected: " + folderName);
                loadCardsFromDeck(folderName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search cards...");

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                spinnerItem.setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                spinnerItem.setVisible(true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                cardAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cardAdapter.filter(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCardsFromDeck(String deckName) {
        List<Card> newCards = new ArrayList<>();
        try {
            String path = "deck/" + deckName + "/cards.json";
            InputStream is = getAssets().open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonBuilder.append(line);

            JSONObject root = new JSONObject(jsonBuilder.toString());
            JSONArray jsonArray = root.getJSONArray("cards");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String en = obj.optString("english");
                String vi = obj.optString("vietnamese");
                String audio = obj.optString("audioFile", null);
                newCards.add(new Card(en, vi, audio));
            }

            Log.d(TAG, "Loaded " + newCards.size() + " cards from deck: " + deckName);
        } catch (Exception e) {
            Log.e(TAG, "Error loading cards from deck: " + deckName, e);
        }

        cardAdapter.updateCards(newCards);
    }
}
