package com.elvinlos.langlo.utils;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.elvinlos.langlo.DeckActivity;
import com.elvinlos.langlo.MainActivity;
import com.elvinlos.langlo.R;
import com.google.android.material.appbar.MaterialToolbar;

public class DrawerHandler {

    /**
     * Handles drawer item clicks for any activity.
     *
     * @param activity    The current activity
     * @param drawerLayout The DrawerLayout to close after handling
     * @param topAppBar   The MaterialToolbar to update titles/menus
     * @param item        The clicked MenuItem
     */
    public static void handleDrawerItem(AppCompatActivity activity,
                                        DrawerLayout drawerLayout,
                                        MaterialToolbar topAppBar,
                                        MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            if (!(activity instanceof MainActivity)) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            }
            topAppBar.setTitle(activity.getString(R.string.home_menu));
            topAppBar.getMenu().clear();
            topAppBar.inflateMenu(R.menu.top_app_bar_home);

        } else if (id == R.id.nav_card) {
            if (!(activity instanceof DeckActivity)) {
                activity.startActivity(new Intent(activity, DeckActivity.class));
                activity.finish();
                topAppBar.getMenu().clear();
                topAppBar.inflateMenu(R.menu.top_app_bar_deck);
            }

        } else if (id == R.id.nav_setting) {
            //TODO : ADD SETTING
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }
}
