package com.elvinlos.langlo;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;
import android.speech.tts.TextToSpeech;
import java.util.Locale;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardStudyActivity extends AppCompatActivity {

    private TextView textCard_en;
    private TextView textCard_vn;
    private MaterialButton btnAction;
    private MaterialButton btnPlayAudio;
    private MediaPlayer mediaPlayer;
    private boolean isPlayingAudio = false;
    private TextToSpeech tts;
    private boolean ttsReady = false;




    private final List<Card> cards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean showingTranslation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_study);

        textCard_en = findViewById(R.id.textCard_en);
        textCard_vn = findViewById(R.id.textCard_vn);
        btnAction = findViewById(R.id.btnAction);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);

        btnPlayAudio.setOnClickListener(v -> {
            if (isPlayingAudio && mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                isPlayingAudio = false;

                btnPlayAudio.setIcon(AppCompatResources.getDrawable(this, R.drawable.play));
            } else {
                String audioFile = cards.get(currentIndex).getAudioFile();
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

        if (!cards.isEmpty()) {
            showCurrentCard();
        }

        btnAction.setOnClickListener(v -> {
            if (!showingTranslation) {
                textCard_vn.setAlpha(1f);
                btnAction.setText("Tiếp theo");
                btnAction.setIcon(AppCompatResources.getDrawable(this, R.drawable.arrow_right));
                btnPlayAudio.setVisibility(View.VISIBLE);
                showingTranslation = true;
            } else {
                currentIndex++;
                if (currentIndex < cards.size()) {
                    showCurrentCard();
                } else {
                    textCard_en.setText("Hết thẻ rồi");
                    textCard_vn.setText("");
                    btnAction.setEnabled(false);
                    btnPlayAudio.setVisibility(View.GONE);
                }
            }
        });
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
        textCard_en.setText(cards.get(currentIndex).getEnglish());
        textCard_vn.setText(cards.get(currentIndex).getVietnamese());

        textCard_vn.setAlpha(0f);
        btnPlayAudio.setVisibility(View.GONE);

        btnAction.setText("Hiện đáp án");
        btnAction.setIcon(AppCompatResources.getDrawable(this, R.drawable.star));
        showingTranslation = false;
    }

    private void playAudio(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            // try to play audio file from assets
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
                isPlayingAudio = true;

                btnPlayAudio.setIcon(AppCompatResources.getDrawable(this, R.drawable.stop));

                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                    isPlayingAudio = false;
                    btnPlayAudio.setIcon(AppCompatResources.getDrawable(this, R.drawable.play));
                });

                return;
            } catch (Exception e) {
                Logger.e("CardStudy", "Error playing audio: " + fileName + ", falling back to TTS", e);
            }
        }

        String text = cards.get(currentIndex).getEnglish();
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
            JSONArray arr = root.getJSONArray("cards");

            Logger.d("CardStudy", "Found " + arr.length() + " cards in JSON");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String english = obj.optString("english");
                String vietnamese = obj.optString("vietnamese");
                String audio = obj.optString("audio");
                cards.add(new Card(english, vietnamese, audio));
            }

            Collections.shuffle(cards);
            Logger.d("CardStudy", "Cards shuffled. Total cards loaded: " + cards.size());

        } catch (Exception e) {
            Logger.e("CardStudy", "Error loading cards for deck: " + deckName, e);
        }
    }
}
