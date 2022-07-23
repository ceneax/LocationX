package ceneax.app.lib.locationx;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LXLog {
    private static final String TAG = "Log_LocationX";
    private static ILogPrinter mLogPrinter;

    public static void setLogPrinter(@Nullable ILogPrinter logPrinter) {
        mLogPrinter = logPrinter;
    }

    public static void d(String msg) {
        if (msg == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
        logPrint(msg);
    }

    public static void i(String msg) {
        if (msg == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, msg);
        }
        logPrint(msg);
    }

    public static void e(String msg) {
        if (msg == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg);
        }
        logPrint(msg);
    }

    private static void logPrint(@NonNull String msg) {
        if (mLogPrinter != null) {
            mLogPrinter.print(msg);
        }
    }
}