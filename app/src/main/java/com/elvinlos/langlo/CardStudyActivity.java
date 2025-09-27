package com.elvinlos.langlo;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CardStudyActivity extends AppCompatActivity {

    private TextView textCardEn;
    private TextView textCardVn;
    private MaterialButton btnAction;
    private MaterialButton btnPlayAudio;

    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private final Set<String> selectedCategories = new HashSet<>();
    private final List<Card> allCards = new ArrayList<>();
    private List<Card> workingCards = Collections.emptyList();
    private final Set<String> categories = new HashSet<>();

    private int currentIndex = 0;
    private boolean showingTranslation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_study);

        initViews();
        initToolbar();

        String deckName = getIntent().getStringExtra("deckName");
        loadCardsFromAssets(deckName);

        if (workingCards.isEmpty()) {
            Toast.makeText(this, "Deck is empty or invalid", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnAction.setOnClickListener(v -> handleActionClick());
        showCurrentCard();
    }

    @Override
    protected void onDestroy() {
        releaseMediaPlayer();
        shutdownTts();
        super.onDestroy();
    }

    private void initViews() {
        textCardEn = findViewById(R.id.textCard_en);
        textCardVn = findViewById(R.id.textCard_vn);
        btnAction = findViewById(R.id.btnAction);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);

        btnPlayAudio.setOnClickListener(v -> toggleAudio());
    }

    private void initToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filter) {
                showCategoryFilterDialog();
                return true;
            } else if (item.getItemId() == R.id.action_clear_filter) {
                clearFilter();
                return true;
            }
            return false;
        });
    }

    private void initTts() {
        tts = new TextToSpeech(this, status -> {
            if (tts != null) return;
            if (status != TextToSpeech.SUCCESS) {
                Logger.e("CardStudy", "TTS initialization failed");
                return;
            }
            int result = tts.setLanguage(Locale.US);
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ttsReady) {
                Logger.d("CardStudy", "TTS initialized successfully");
            }
        });
    }

    private void handleActionClick() {
        if (!showingTranslation) {
            textCardVn.setAlpha(1f);
            btnAction.setText(R.string.btn_next);
            btnPlayAudio.setVisibility(View.VISIBLE);
            showingTranslation = true;
            return;
        }

        currentIndex++;
        if (currentIndex < workingCards.size()) {
            showCurrentCard();
        } else {
            textCardEn.setText(R.string.out_of_card);
            textCardVn.setText("");
            btnAction.setEnabled(false);
            btnPlayAudio.setVisibility(View.GONE);
        }
    }

    private void showCurrentCard() {
        if (workingCards.isEmpty() || currentIndex >= workingCards.size()) return;

        Card card = workingCards.get(currentIndex);
        textCardEn.setText(card.getEnglish());
        textCardVn.setText(card.getVietnamese());

        textCardVn.setAlpha(0f);
        btnPlayAudio.setVisibility(View.GONE);
        btnAction.setText(R.string.btn_show_answer);
        btnAction.setIcon(AppCompatResources.getDrawable(this, R.drawable.star));
        showingTranslation = false;
    }

    private void toggleAudio() {
        initTts();

        if (mediaPlayer != null) {
            releaseMediaPlayer();
        } else {
            playAudio(workingCards.get(currentIndex).getAudioFile());
        }
    }

    private void playAudio(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            try {
                releaseMediaPlayer();

                AssetManager assetManager = getAssets();
                String deckName = getIntent().getStringExtra("deckName");
                AssetFileDescriptor afd = assetManager.openFd("deck/" + deckName + "/Audio/" + fileName);

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
                return;
            } catch (Exception e) {
                Logger.e("CardStudy", "Error playing audio: " + fileName + ", falling back to TTS", e);
            }
        }

        speakWithTts(workingCards.get(currentIndex).getEnglish());
    }

    private void speakWithTts(String text) {
        if (ttsReady && tts != null && text != null && !text.isEmpty()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CardTTS");
            Logger.d("CardStudy", "Speaking with TTS: " + text);
        } else {
            Logger.d("CardStudy", "TTS not ready, cannot speak.");
        }
    }

    private void loadCardsFromAssets(String deckName) {
        try (InputStream is = getAssets().open("deck/" + deckName + "/cards.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONArray arr = new JSONObject(jsonBuilder.toString()).optJSONArray("cards");
            if (arr == null || arr.length() == 0) {
                Logger.e("CardStudy", "Deck has no cards");
                return;
            }

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;

                String english = obj.optString("english", "");
                String vietnamese = obj.optString("vietnamese", "");
                String audio = obj.optString("audio", "");
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
                Logger.d("CardStudy", "Cards shuffled. Total loaded: " + allCards.size());
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

        List<String> sortedCategories = new ArrayList<>(categories);
        sortedCategories.sort(String.CASE_INSENSITIVE_ORDER);
        String[] categoryArray = sortedCategories.toArray(new String[0]);
        boolean[] checkedItems = new boolean[categoryArray.length];

        for (int i = 0; i < categoryArray.length; i++) {
            checkedItems[i] = selectedCategories.contains(categoryArray[i]);
        }

        new MaterialAlertDialogBuilder(this, R.style.CustomMaterialAlertDialog)
                .setTitle(R.string.select_categories)
                .setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    selectedCategories.clear();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) selectedCategories.add(categoryArray[i]);
                    }
                    applyCategoryFilter(new ArrayList<>(selectedCategories));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void applyCategoryFilter(List<String> categoriesToApply) {
        workingCards = categoriesToApply.isEmpty()
                ? new ArrayList<>(allCards)
                : filterCardsByCategories(categoriesToApply);

        if (!workingCards.isEmpty()) {
            Collections.shuffle(workingCards);
            currentIndex = 0;
            showCurrentCard();
            Toast.makeText(this, R.string.filter_applied, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.filter_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private List<Card> filterCardsByCategories(List<String> categoriesToApply) {
        List<Card> filtered = new ArrayList<>();
        for (Card card : allCards) {
            if (categoriesToApply.contains(card.getCategory())) {
                filtered.add(card);
            }
        }
        return filtered;
    }

    private void clearFilter() {
        workingCards = new ArrayList<>(allCards);
        Collections.shuffle(workingCards);
        currentIndex = 0;
        showCurrentCard();
        Toast.makeText(this, "Filter cleared", Toast.LENGTH_SHORT).show();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void shutdownTts() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
