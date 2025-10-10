package com.elvinlos.langlo.ui.deck;

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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.Card;
import com.elvinlos.langlo.ui.cardstudy.CardAdapter;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.utils.DrawerHandler;
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

    private DrawerLayout drawerLayout;
    private MaterialToolbar topAppBar;
    private ActionBarDrawerToggle toggle;
    private CardAdapter cardAdapter;

    private final List<Card> allCards = new ArrayList<>();
    private final List<String> deckFolders = new ArrayList<>();
    private final List<String> deckTitles = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        drawerLayout = findViewById(R.id.drawerLayoutDeck);
        NavigationView navigationView = findViewById(R.id.navigationViewDeck);
        topAppBar = findViewById(R.id.topAppBarDeck);
        RecyclerView recyclerViewCards = findViewById(R.id.recyclerViewCards);

        setSupportActionBar(topAppBar);

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

        recyclerViewCards.setLayoutManager(new LinearLayoutManager(this));
        cardAdapter = new CardAdapter();
        recyclerViewCards.setAdapter(cardAdapter);

        loadDeckFoldersAndTitles();

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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_deck, menu);

        MenuItem spinnerItem = menu.findItem(R.id.action_deck_spinner);
        View spinnerView = spinnerItem.getActionView();
        if (spinnerView instanceof Spinner) {
            Spinner spinner = (Spinner) spinnerView;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, deckTitles);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                    String folderName = deckFolders.get(position);
                    Log.d(TAG, "Spinner selected: " + folderName);
                    loadCardsFromDeck(folderName);
                }

                @Override
                public void onNothingSelected(@NonNull AdapterView<?> parent) {
                }
            });
        }

        MenuItem searchItem = menu.findItem(R.id.action_search);
        View actionView = searchItem.getActionView();
        if (actionView instanceof androidx.appcompat.widget.SearchView) {
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) actionView;
            searchView.setQueryHint("Search cards...");

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                    spinnerItem.setVisible(false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                    spinnerItem.setVisible(true);
                    return true;
                }
            });

            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(@NonNull String query) {
                    filterCards(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(@NonNull String newText) {
                    filterCards(newText);
                    return true;
                }
            });
        }

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
            JSONArray jsonArray = root.optJSONArray("cards");

            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.optJSONObject(i);
                    if (obj == null) continue;

                    String en = obj.optString("english", "");
                    String vi = obj.optString("vietnamese", "");
                    String audio = obj.optString("audioFile", "");

                    if (!en.isEmpty() && !vi.isEmpty()) {
                        newCards.add(new Card(String.valueOf(i), en, vi, audio));
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading cards from deck: " + deckName, e);
        }

        allCards.clear();
        allCards.addAll(newCards);

        cardAdapter.submitList(new ArrayList<>(allCards));
    }

    private void filterCards(@Nullable String query) {
        if (query == null || query.trim().isEmpty()) {
            cardAdapter.submitList(new ArrayList<>(allCards));
        } else {
            String lower = query.toLowerCase();
            List<Card> filtered = new ArrayList<>();
            for (Card c : allCards) {
                if (c.getEnglish().toLowerCase().contains(lower) ||
                        c.getVietnamese().toLowerCase().contains(lower)) {
                    filtered.add(c);
                }
            }
            cardAdapter.submitList(filtered);
        }
    }
}
