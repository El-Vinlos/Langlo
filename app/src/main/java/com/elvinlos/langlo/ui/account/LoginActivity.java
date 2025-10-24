package com.elvinlos.langlo.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.elvinlos.langlo.FirebaseHelper;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.User;
import com.elvinlos.langlo.ui.main.MainActivity;
import com.elvinlos.langlo.utils.Navigation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> Navigation.navigateToActivity(this, MainActivity.class));

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
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID is null or empty!");
            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (playersRef == null) {
            Log.e(TAG, "DatabaseReference is null!");
            Toast.makeText(this, "Lỗi kết nối database", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo EditText với padding
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập tên của bạn");
        if (defaultName != null && !defaultName.isEmpty()) {
            input.setText(defaultName);
        }

        // Set padding cho EditText
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        // Sử dụng MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomMaterialAlertDialog);
        builder.setTitle("Chào mừng!");
        builder.setMessage("Vui lòng nhập tên hiển thị của bạn:");
        builder.setView(input);
        builder.setCancelable(false);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = input.getText().toString().trim();

            // Nếu không nhập gì thì dùng default
            if (name.isEmpty()) {
                name = defaultName != null && !defaultName.isEmpty() ? defaultName : "Player";
            }

            // Tạo User object với username và totalScore = 0
            User newPlayer = new User(name, 0);
            String finalName = name;

            Log.d(TAG, "Attempting to save - UID: " + uid + ", Username: " + finalName);

            // Lưu lên Firebase
            playersRef.child(uid).setValue(newPlayer)
                    .addOnSuccessListener(v -> {
                        Log.d(TAG, "✅ Player saved successfully!");
                        Log.d(TAG, "Username: " + finalName);
                        Log.d(TAG, "Path: players/" + uid);
                        Toast.makeText(this, "Đã lưu: " + finalName, Toast.LENGTH_SHORT).show();

                        if (onComplete != null) {
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to save player: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        builder.show();
    }

}