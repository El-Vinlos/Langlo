package com.elvinlos.langlo;

import android.util.Log;
import com.elvinlos.langlo.BuildConfig;

public class Logger {
    // BuildConfig.DEBUG is true in Debug builds, false in Release builds
    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.e(tag, msg, t);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }
}
