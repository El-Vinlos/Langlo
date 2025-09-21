package com.elvinlos.langlo;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CardStudyActivity extends AppCompatActivity {

    private TextView textCard_en;
    private TextView textCard_vn;
    private MaterialButton btnAction;
    private MaterialButton btnPlayAudio;
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private final List<Card> allCards = new ArrayList<>();   // full deck
    private List<Card> workingCards = new ArrayList<>();     // filtered/shuffled deck
    private int currentIndex = 0;
    private boolean showingTranslation = false;
    private final Set<String> categories = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_study);

        textCard_en = findViewById(R.id.textCard_en);
        textCard_vn = findViewById(R.id.textCard_vn);
        btnAction = findViewById(R.id.btnAction);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);

        toolbar.setNavigationOnClickListener(v -> finish());

        btnPlayAudio.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            } else {
                String audioFile = workingCards.get(currentIndex).getAudioFile();
                playAudio(audioFile);
            }
        });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Logger.e("CardStudy", "TTS language not supported");
                } else {
                    ttsReady = true;
                    Logger.d("CardStudy", "TTS initialized successfully");
                }
            } else {
                Logger.e("CardStudy", "TTS initialization failed");
            }
        });

        String deckName = getIntent().getStringExtra("deckName");
        loadCardsFromAssets(deckName);

        if (workingCards.isEmpty()) {
            Toast.makeText(this, "Deck is empty or invalid", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnAction.setOnClickListener(v -> {
            if (!showingTranslation) {
                textCard_vn.setAlpha(1f);
                btnAction.setText(R.string.btn_next);
                btnAction.setIcon(AppCompatResources.getDrawable(this, R.drawable.arrow_right));
                btnPlayAudio.setVisibility(View.VISIBLE);
                showingTranslation = true;
            } else {
                currentIndex++;
                if (currentIndex < workingCards.size()) {
                    showCurrentCard();
                } else {
                    textCard_en.setText(R.string.out_of_card);
                    textCard_vn.setText("");
                    btnAction.setEnabled(false);
                    btnPlayAudio.setVisibility(View.GONE);
                }
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_filter) {
                showCategoryFilterDialog();
                return true;
            } else if (id == R.id.action_clear_filter) {
                workingCards = new ArrayList<>(allCards);
                Collections.shuffle(workingCards);
                currentIndex = 0;
                showCurrentCard();
                Toast.makeText(this, "Filter cleared", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        showCurrentCard();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void showCurrentCard() {
        if (workingCards.isEmpty() || currentIndex >= workingCards.size()) return;

        Card card = workingCards.get(currentIndex);
        textCard_en.setText(card.getEnglish());
        textCard_vn.setText(card.getVietnamese());

        textCard_vn.setAlpha(0f);
        btnPlayAudio.setVisibility(View.GONE);

        btnAction.setText(R.string.btn_show_answer);
        btnAction.setIcon(AppCompatResources.getDrawable(this, R.drawable.star));
        showingTranslation = false;
    }

    private void playAudio(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }

                AssetManager assetManager = getAssets();
                String deckName = getIntent().getStringExtra("deckName");
                AssetFileDescriptor afd = assetManager.openFd("deck/" + deckName + "/audio/" + fileName);

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.prepare();
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                });

                return;
            } catch (Exception e) {
                Logger.e("CardStudy", "Error playing audio: " + fileName + ", falling back to TTS", e);
            }
        }

        String text = workingCards.get(currentIndex).getEnglish();
        if (ttsReady && tts != null && text != null && !text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CardTTS");
            Logger.d("CardStudy", "Speaking with TTS: " + text);
        } else {
            Logger.d("CardStudy", "TTS not ready, cannot speak.");
        }
    }

    private void loadCardsFromAssets(String deckName) {
        try {
            AssetManager assetManager = getAssets();
            String path = "deck/" + deckName + "/cards.json";

            Logger.d("CardStudy", "Loading cards from: " + path);

            InputStream is = assetManager.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject root = new JSONObject(jsonBuilder.toString());
            JSONArray arr = root.optJSONArray("cards");

            if (arr == null || arr.length() == 0) {
                Logger.e("CardStudy", "Deck has no cards, skipping load");
                return;
            }

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;

                String english = obj.optString("english", "");
                String vietnamese = obj.optString("vietnamese", "");
                String audio = obj.optString("audio", null);
                String category = obj.optString("category", "General");

                if (english.isEmpty() || vietnamese.isEmpty()) {
                    Logger.w("CardStudy", "Skipping invalid card at index " + i);
                    continue;
                }

                Card card = new Card(String.valueOf(i), english, vietnamese, audio);
                card.setCategory(category);
                allCards.add(card);
                categories.add(category);
            }

            if (!allCards.isEmpty()) {
                workingCards = new ArrayList<>(allCards);
                Collections.shuffle(workingCards);
                Logger.d("CardStudy", "Cards shuffled. Total cards loaded: " + allCards.size());
            } else {
                Logger.e("CardStudy", "No valid cards loaded from JSON");
            }

        } catch (Exception e) {
            Logger.e("CardStudy", "Error loading cards for deck: " + deckName, e);
        }
    }

    private void showCategoryFilterDialog() {
        if (categories.isEmpty()) {
            Toast.makeText(this, R.string.no_categories_available, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryArray = categories.toArray(new String[0]);
        boolean[] checkedItems = new boolean[categoryArray.length];

        new MaterialAlertDialogBuilder(this, R.style.CustomMaterialAlertDialog)
                .setTitle(R.string.select_categories)
                .setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    List<String> selectedCategories = new ArrayList<>();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selectedCategories.add(categoryArray[i]);
                        }
                    }
                    applyCategoryFilter(selectedCategories);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void applyCategoryFilter(List<String> selectedCategories) {
        if (selectedCategories.isEmpty()) {
            workingCards = new ArrayList<>(allCards);
        } else {
            workingCards = new ArrayList<>();
            for (Card card : allCards) {
                if (selectedCategories.contains(card.getCategory())) {
                    workingCards.add(card);
                }
            }
        }

        if (!workingCards.isEmpty()) {
            Collections.shuffle(workingCards);
            currentIndex = 0;
            showCurrentCard();
            Toast.makeText(this, R.string.filter_applied, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.filter_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
