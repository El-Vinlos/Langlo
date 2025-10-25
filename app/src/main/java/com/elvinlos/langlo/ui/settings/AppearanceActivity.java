package com.elvinlos.langlo.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.elvinlos.langlo.R;

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

        RadioGroup themeRadioGroup = findViewById(R.id.theme_radio_group);
        RadioButton lightThemeRadio = findViewById(R.id.light_theme_radio);
        RadioButton darkThemeRadio = findViewById(R.id.dark_theme_radio);

        // Load saved theme preference
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
            }
        });
    }

    private void applyTheme(String theme) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("theme", theme);
        editor.apply();

        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
