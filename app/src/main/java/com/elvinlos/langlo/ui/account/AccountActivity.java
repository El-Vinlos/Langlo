package com.elvinlos.langlo.ui.account;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.bumptech.glide.Glide;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.ui.main.MainActivity;
import com.elvinlos.langlo.utils.Navigation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executors;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";

    // Declare UI elements
    private TextView nameTextView;
    private TextView emailTextView;
    private ImageView avatarImageView;
    private MaterialButton signOutButton;
    private MaterialCardView editProfileCard;
    private MaterialCardView settingsCard;

    // Firebase components
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> Navigation.navigateToActivity(this, MainActivity.class));

        // Initialize UI elements
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        avatarImageView = findViewById(R.id.avatarImageView);
        signOutButton = findViewById(R.id.signOutButton);
        editProfileCard = findViewById(R.id.editProfileCard);
        settingsCard = findViewById(R.id.settingsCard);

        // Set user information
        setUserInfo();

        // Set click listeners
        signOutButton.setOnClickListener(v -> signOut());

        editProfileCard.setOnClickListener(v -> {
            // TODO: Navigate to edit profile activity
            Log.d(TAG, "Edit profile clicked");
        });

        settingsCard.setOnClickListener(v -> {
            // TODO: Navigate to settings activity
            Log.d(TAG, "Settings clicked");
        });
    }

    private void setUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Set email
            String email = currentUser.getEmail();
            if (email != null) {
                emailTextView.setText(email);
            } else {
                emailTextView.setText("");
            }

            // Load avatar from Google account
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_launcher_foreground) // Default placeholder
                        .circleCrop()
                        .into(avatarImageView);
            }

            // Get display name from Firebase Realtime Database
            FirebaseDatabase db = FirebaseDatabase.getInstance(
                    "https://langlo-7c380-default-rtdb.asia-southeast1.firebasedatabase.app/"
            );

            db.getReference("users").child(uid).child("name").get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String userName = snapshot.getValue(String.class);
                            nameTextView.setText(userName);
                        } else {
                            nameTextView.setText("Welcome!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get user name", e);
                        nameTextView.setText("Welcome!");
                    });
        } else {
            // No user logged in, navigate to login
            Navigation.navigateToActivity(AccountActivity.this, LoginActivity.class);
            finish();
        }
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Clear credential state from all credential providers
        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
                clearRequest,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(@NonNull Void result) {
                        Log.d(TAG, "Credential state cleared successfully");
                        runOnUiThread(() -> {
                            Navigation.navigateToActivity(AccountActivity.this, MainActivity.class);
                        });
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e(TAG, "Error clearing credential state: " + e.getLocalizedMessage());
                        // Still navigate even if clearing fails
                        runOnUiThread(() -> {
                            Navigation.navigateToActivity(AccountActivity.this, MainActivity.class);
                        });
                    }
                });
    }
}