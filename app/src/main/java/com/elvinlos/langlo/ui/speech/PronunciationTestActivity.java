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

import com.elvinlos.langlo.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Locale;

public class PronunciationTestActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 101;

    private MaterialToolbar toolbar;
    private TextView wordToSpeakText;
    private TextView instructionText;
    private TextView resultText;
    private TextView scoreText;
    private Button startButton;
    private Button btn_retry;
    private ProgressBar progressBar;
    private MaterialCardView resultCard;

    private SpeechRecognizer speechRecognizer;
    private String currentWord;
    private float bestScore = 0f;
    private int attemptCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pronunciation_activity);

        initViews();
        setupToolbar();

        // Get word from intent or use default
        currentWord = getIntent().getStringExtra("WORD_TO_TEST");
        if (currentWord == null || currentWord.isEmpty()) {
            currentWord = "Hello";
        }

        // Request permission immediately when activity starts
        requestAudioPermission();
        setupSpeechRecognizer();
        displayWord();
    }

    private void requestAudioPermission() {
        if (!hasAudioPermission()) {
            // Show rationale if needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Microphone Permission Required")
                        .setMessage("This app needs microphone access to test your pronunciation. Please allow microphone permission to continue.")
                        .setPositiveButton("Allow", (dialog, which) -> ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                REQUEST_RECORD_AUDIO))
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .show();
            } else {
                // First time - directly request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO);
            }
        }
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        wordToSpeakText = findViewById(R.id.wordToSpeak);
        instructionText = findViewById(R.id.instructionText);
        resultText = findViewById(R.id.resultText);
        scoreText = findViewById(R.id.scoreText);
        startButton = findViewById(R.id.startButton);
        btn_retry = findViewById(R.id.retry_button);
        progressBar = findViewById(R.id.progressBar);
        resultCard = findViewById(R.id.resultCard);

        startButton.setOnClickListener(v -> startListening());
        btn_retry.setOnClickListener(v -> retryPronunciation());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pronunciation Test");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayWord() {
        wordToSpeakText.setText(currentWord);

        if (hasAudioPermission()) {
            instructionText.setText(R.string.tap_mic_to_start);
            startButton.setEnabled(true);
        } else {
            instructionText.setText(R.string.wait_for_mic_perm);
            startButton.setEnabled(false);
        }

        resultCard.setVisibility(View.GONE);
        btn_retry.setVisibility(View.GONE);
    }

    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
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
                instructionText.setText(R.string.pronounce_intruction);
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
                instructionText.setText(R.string.comparing_result_str);
            }

            @Override
            public void onError(int error) {
                progressBar.setVisibility(View.GONE);
                startButton.setEnabled(true);
                String errorMessage = getErrorMessage(error);
                instructionText.setText(getString(R.string.error) + errorMessage);
                Toast.makeText(PronunciationTestActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                progressBar.setVisibility(View.GONE);
                startButton.setEnabled(true);

                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                float[] confidenceScores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                if (matches != null && !matches.isEmpty()) {
                    String spokenWord = matches.get(0);
                    float confidence = (confidenceScores != null && confidenceScores.length > 0)
                            ? confidenceScores[0] : 0f;
                    checkPronunciation(spokenWord, confidence);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (!hasAudioPermission()) {
            Toast.makeText(this, "Please grant microphone permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
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

    private void checkPronunciation(String spokenWord, float confidence) {
        attemptCount++;

        float pronunciationScore;
        boolean isExactMatch = spokenWord.trim().equalsIgnoreCase(currentWord.trim());

        if (isExactMatch) {
            pronunciationScore = confidence * 100f; // Convert to percentage
        } else {
            pronunciationScore = calculateSimilarity(currentWord, spokenWord) * confidence * 100f;
        }

        if (pronunciationScore > bestScore) {
            bestScore = pronunciationScore;
        }

        int percentage = Math.round(pronunciationScore);
        String resultMessage;
        int color;

        if (percentage >= 85) {
            resultMessage = getString(R.string.speech_points_excellent) + percentage + "%)";
            color = getColor(android.R.color.holo_green_dark);
        } else if (percentage >= 70) {
            resultMessage = getString(R.string.speech_points_good) + percentage + "%)";
            color = getColor(android.R.color.holo_green_light);
        } else if (percentage >= 50) {
            resultMessage = getString(R.string.speech_points_fair) + percentage + "%)";
            color = getColor(android.R.color.holo_orange_light);
        } else {
            resultMessage = getString(R.string.speech_points_poor) + percentage + "%)";
            color = getColor(android.R.color.holo_red_dark);
        }

        resultText.setText(resultMessage);
        resultText.setTextColor(color);

        updateScore();
        resultCard.setVisibility(View.VISIBLE);
        btn_retry.setVisibility(View.VISIBLE);
        btn_retry.setText(R.string.btn_retry_text);
        startButton.setVisibility(View.GONE);

        String instruction = percentage >= 85 ? "Phát âm tốt!" :
                percentage >= 70 ? "Good job! Try again for better score." :
                        "Keep practicing!";
        instructionText.setText(instruction);
    }

    private void retryPronunciation() {
        resultCard.setVisibility(View.GONE);
        btn_retry.setVisibility(View.GONE);
        startButton.setVisibility(View.VISIBLE);
        instructionText.setText(R.string.recording_intruct);

        startListening();
    }

    private float calculateSimilarity(String target, String spoken) {
        target = target.toLowerCase();
        spoken = spoken.toLowerCase();

        // Sự dụng thuật toán Levenshtein
        int maxLen = Math.max(target.length(), spoken.length());
        if (maxLen == 0) return 1.0f;

        int distance = levenshteinDistance(target, spoken);
        return 1.0f - ((float) distance / maxLen);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private void updateScore() {
        int percentage = Math.round(bestScore);
        scoreText.setText(getString(R.string.best_score_scoretext) + percentage + "% ( " + getString(R.string.attempts_score_text) + attemptCount + " )");
    }

    private String getErrorMessage(int error) {
        return switch (error) {
            case SpeechRecognizer.ERROR_AUDIO -> getString(R.string.audio_recording_error);
            case SpeechRecognizer.ERROR_CLIENT -> getString(R.string.client_side_error);
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                    getString(R.string.insufficient_permissions);
            case SpeechRecognizer.ERROR_NETWORK -> getString(R.string.network_error);
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> getString(R.string.network_timeout);
            case SpeechRecognizer.ERROR_NO_MATCH -> getString(R.string.no_match_found_try_again);
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                    getString(R.string.recognition_service_busy);
            case SpeechRecognizer.ERROR_SERVER -> getString(R.string.server_error);
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> getString(R.string.no_speech_input);
            default -> getString(R.string.unknown_error);
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_granted_toast, Toast.LENGTH_SHORT).show();
                startButton.setEnabled(true);
                instructionText.setText(R.string.tap_mic_to_start);
            } else {
                Toast.makeText(this, R.string.ask_mic_perm_toast, Toast.LENGTH_SHORT).show();
                startButton.setEnabled(false);
                instructionText.setText(R.string.mic_permission_denied);
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