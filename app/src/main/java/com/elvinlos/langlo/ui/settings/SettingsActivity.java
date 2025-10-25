package com.elvinlos.langlo.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.elvinlos.langlo.R;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout appearanceItem = findViewById(R.id.appearance_item);
        appearanceItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AppearanceActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
