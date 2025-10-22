package com.elvinlos.langlo.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.elvinlos.langlo.R;
import com.elvinlos.langlo.ui.main.MainActivity;
import com.elvinlos.langlo.utils.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executors;

public class AccountActivity extends AppCompatActivity {

    // Declare UI elements and Firebase components
    private TextView nameTextView;
    private Button signOutButton;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);


        nameTextView = findViewById(R.id.nameTextView);
        signOutButton = findViewById(R.id.signOutButton);


        setUserName();


        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void setUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Lấy tên từ Database
            FirebaseDatabase db = FirebaseDatabase.getInstance("https://langlo-7c380-default-rtdb.asia-southeast1.firebasedatabase.app/");
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
                        Log.e("AccountActivity", "Failed to get user name", e);
                        nameTextView.setText("Welcome!");
                    });
        } else {
            Navigation.navigateToActivity(AccountActivity.this, LoginActivity.class);
            finish();
        }
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // When a user signs out, clear the current user credential state from all credential providers.
        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
                clearRequest,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(@NonNull Void result) { Navigation.navigateToActivity(AccountActivity.this, MainActivity.class); }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        Log.e("AccountActivity", "Error clearing credential state" + e.getLocalizedMessage());
                    }
                });
    }


}
