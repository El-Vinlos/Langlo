package com.elvinlos.langlo.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.elvinlos.langlo.R;
import com.google.android.material.appbar.MaterialToolbar;

public class AppearanceActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Appearance");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(v -> finish());

        RadioGroup themeRadioGroup = findViewById(R.id.theme_radio_group);
        RadioButton lightThemeRadio = findViewById(R.id.light_theme_radio);
        RadioButton darkThemeRadio = findViewById(R.id.dark_theme_radio);

        String savedTheme = prefs.getString("theme", "light");
        if (savedTheme.equals("dark")) {
            darkThemeRadio.setChecked(true);
        } else {
            lightThemeRadio.setChecked(true);
        }

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.light_theme_radio) {
                applyTheme("light");
            } else if (checkedId == R.id.dark_theme_radio) {
                applyTheme("dark");
            } else if (checkedId == R.id.system_theme_radio) {
                applyTheme("system");
            }
        });
    }

    private void applyTheme(String theme) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("theme", theme);
        editor.apply();

        switch (theme) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
