package com.elvinlos.langlo.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.elvinlos.langlo.R;
import com.google.android.material.appbar.MaterialToolbar;

import android.content.Intent;
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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        LinearLayout appearanceItem = findViewById(R.id.appearance_item);
        appearanceItem.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AppearanceActivity.class);
            startActivity(intent);
        });
    }
}
