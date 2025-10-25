package com.elvinlos.langlo.utils;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.elvinlos.langlo.ui.account.LoginActivity;
import com.elvinlos.langlo.ui.deck.DeckActivity;
import com.elvinlos.langlo.ui.exam.ExamActivity;
import com.elvinlos.langlo.ui.exam.ExamSelectorActivity;
import com.elvinlos.langlo.ui.main.MainActivity;
import com.elvinlos.langlo.R;
import com.elvinlos.langlo.ui.speech.SpeakActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

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
        }
//        else if (id == R.id.nav_exam) {
//            if (!isUserLoggedIn(activity)) {
//                showLoginRequiredDialog(activity);
//            } else if (!(activity instanceof ExamActivity)) {
//                activity.startActivity(new Intent(activity, ExamSelectorActivity.class));
//                activity.finish();
//                topAppBar.getMenu().clear();
//                topAppBar.inflateMenu(R.menu.top_app_bar_deck);
//            }
//        }
        else if (id == R.id.nav_exam) {
            if (!(activity instanceof ExamSelectorActivity)) {
                activity.startActivity(new Intent(activity, ExamSelectorActivity.class));
                activity.finish();
                topAppBar.getMenu().clear();
                topAppBar.inflateMenu(R.menu.top_app_bar_deck);
            }
        } else if (id == R.id.nav_setting) {
            //TODO : ADD SETTING
        } else if (id == R.id.nav_speak) {
            if (!(activity instanceof SpeakActivity)) {
                activity.startActivity(new Intent(activity, SpeakActivity.class));
                activity.finish();
                topAppBar.getMenu().clear();
                topAppBar.inflateMenu(R.menu.top_app_bar_deck);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập chưa
     *
     * @param context Context để khởi tạo FirebaseAuth
     * @return true nếu đã đăng nhập, false nếu chưa
     */
    private static boolean isUserLoggedIn(Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Hiển thị dialog yêu cầu đăng nhập
     *
     * @param activity Activity hiện tại
     */
    private static void showLoginRequiredDialog(AppCompatActivity activity) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.CustomMaterialAlertDialog);
        builder.setTitle("Yêu cầu đăng nhập");
        builder.setMessage("Bạn cần đăng nhập để làm bài kiểm tra!");
        builder.setPositiveButton("Đăng nhập", (dialog, which) -> {
            // Điều hướng đến màn hình đăng nhập
            Navigation.navigateToActivity(activity, LoginActivity.class);
        });
        builder.setNegativeButton("Hủy", null);
        builder.setCancelable(true);
        builder.show();
    }
}