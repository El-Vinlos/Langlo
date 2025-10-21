package com.elvinlos.langlo.ui.speech;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.elvinlos.langlo.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PronunciationTestActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 101;

    private MaterialToolbar toolbar;
    private TextView wordToSpeakText;
    private TextView instructionText;
    private TextView resultText;
    private TextView scoreText;
    private Button startButton;
    private Button nextButton;
    private ProgressBar progressBar;
    private MaterialCardView resultCard;

    private SpeechRecognizer speechRecognizer;
    private List<String> testWords;
    private int currentWordIndex = 0;
    private int correctCount = 0;
    private int totalAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pronunciation_activity);

        initViews();
        setupToolbar();
        initTestWords();
        checkPermissions();
        setupSpeechRecognizer();
        loadNextWord();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        wordToSpeakText = findViewById(R.id.wordToSpeak);
        instructionText = findViewById(R.id.instructionText);
        resultText = findViewById(R.id.resultText);
        scoreText = findViewById(R.id.scoreText);
        startButton = findViewById(R.id.startButton);
        nextButton = findViewById(R.id.nextButton);
        progressBar = findViewById(R.id.progressBar);
        resultCard = findViewById(R.id.resultCard);

        startButton.setOnClickListener(v -> startListening());
        nextButton.setOnClickListener(v -> loadNextWord());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pronunciation Test");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initTestWords() {
        testWords = Arrays.asList(
                "Hello",
                "Pronunciation",
                "Beautiful",
                "Wednesday",
                "Restaurant",
                "Library",
                "Chocolate",
                "Temperature"
        );
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
    }

    private void setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                instructionText.setText("Listening... Speak now!");
            }

            @Override
            public void onBeginningOfSpeech() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                progressBar.setVisibility(View.GONE);
                instructionText.setText("Processing...");
            }

            @Override
            public void onError(int error) {
                progressBar.setVisibility(View.GONE);
                startButton.setEnabled(true);
                String errorMessage = getErrorMessage(error);
                instructionText.setText("Error: " + errorMessage);
                Toast.makeText(PronunciationTestActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                progressBar.setVisibility(View.GONE);
                startButton.setEnabled(true);

                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    checkPronunciation(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        startButton.setEnabled(false);
        resultCard.setVisibility(View.GONE);
        speechRecognizer.startListening(intent);
    }

    private void checkPronunciation(String spokenWord) {
        totalAttempts++;
        String targetWord = testWords.get(currentWordIndex);

        boolean isCorrect = spokenWord.trim().equalsIgnoreCase(targetWord.trim());

        if (isCorrect) {
            correctCount++;
            resultText.setText("✓ Correct! You said: " + spokenWord);
            resultText.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            resultText.setText("✗ You said: " + spokenWord + "\nExpected: " + targetWord);
            resultText.setTextColor(getColor(android.R.color.holo_red_dark));
        }

        updateScore();
        resultCard.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        instructionText.setText(isCorrect ? "Great job!" : "Try the next word!");
    }

    private void loadNextWord() {
        currentWordIndex++;
        if (currentWordIndex >= testWords.size()) {
            currentWordIndex = 0; // Loop back to start
        }

        wordToSpeakText.setText(testWords.get(currentWordIndex));
        instructionText.setText("Tap the microphone and say the word above");
        resultCard.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        startButton.setEnabled(true);
    }

    private void updateScore() {
        int percentage = totalAttempts > 0 ? (correctCount * 100) / totalAttempts : 0;
        scoreText.setText("Score: " + correctCount + "/" + totalAttempts + " (" + percentage + "%)");
    }

    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match found. Try again!";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot use speech recognition.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}