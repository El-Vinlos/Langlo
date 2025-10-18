package com.elvinlos.langlo.utils;

import android.app.Activity;
import android.content.Intent;

public class Navigation {
    public static void navigateToActivity(Activity src, Class<?> destClass) {
        Intent intent = new Intent(src, destClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        src.startActivity(intent);
        src.finish();
    }

    public static void navigateToActivityWithBackStack(Activity src, Class<?> destClass) {
        Intent intent = new Intent(src, destClass);
        src.startActivity(intent);
    }
}
