package com.elvinlos.langlo.ui.account;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.elvinlos.langlo.FirebaseHelper;
import com.elvinlos.langlo.R;
import com.google.firebase.auth.FirebaseUser;

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
        firebaseHelper.launchSignIn();
        // TODO: add user activity
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseHelper.checkSignIn();
        if (user != null) {
            finish();
        }
    }
}
