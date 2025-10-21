package com.elvinlos.langlo.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.elvinlos.langlo.FirebaseHelper;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.User;
import com.elvinlos.langlo.utils.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseHelper = new FirebaseHelper(this);

        Button googleSignInBtn = findViewById(R.id.btn_google_signin);
        googleSignInBtn.setOnClickListener(v -> logIn());
    }

    private void logIn() {
        firebaseHelper.launchSignIn(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


            createOrReuseUser(user, () -> Navigation.navigateToActivityWithBackStack(LoginActivity.this, AccountActivity.class));
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseHelper.checkSignIn();
        if (user != null) {
            finish();
        }
    }
    public void createOrReuseUser(FirebaseUser user, Runnable onComplete) {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://langlo-7c380-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference playersRef = db.getReference("users");

        String uid = user.getUid();
        String googleName = user.getDisplayName();

        playersRef.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    // Ask user to input their preferred name
                    promptForName(uid, googleName, playersRef, onComplete);
                } else {
                    Log.d(TAG, "Existing player found");
                    onComplete.run(); // <-- Chỉ điều hướng sau khi xong
                }
            } else {
                Log.e(TAG, "Failed to check player existence", task.getException());
            }
        });
    }

    private void promptForName(String uid, String defaultName, DatabaseReference playersRef, Runnable onComplete) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter your name");
        if (defaultName != null) input.setText(defaultName);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Welcome!")
                .setMessage("Please enter your display name:")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = defaultName != null ? defaultName : "Player";

                    User newPlayer = new User(name, 0);
                    String finalName = name;
                    playersRef.child(uid).setValue(newPlayer)
                            .addOnSuccessListener(v -> {
                                Log.d(TAG, "New player created: " + finalName);
                                onComplete.run(); // <-- Điều hướng sau khi lưu xong
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to add player", e));
                })
                .show();
    }

}