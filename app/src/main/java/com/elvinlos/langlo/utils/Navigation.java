package com.elvinlos.langlo.utils;

import android.app.Activity;
import android.content.Intent;

import com.elvinlos.langlo.ui.account.LoginActivity;
import com.elvinlos.langlo.ui.main.MainActivity;

public class Navigation {
    public static void navigateToActivity(Activity src, Class<?> destClass) {
        Intent intent = new Intent(src, destClass);
        // Add flags to clear the activity stack and prevent the user from going back to the account screen
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        src.startActivity(intent);
        src.finish();
    }


}
