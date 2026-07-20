package com.caint.api;

import android.util.Log;

/**
 * Caint.Dialogs
 *
 * Surface for showing simple native-style dialogs without exposing
 * Telegram's internal dialog/alert classes.
 *
 * All methods are placeholders for now.
 */
public final class CaintDialogs {

    private static final String TAG = "Caint.Dialogs";

    CaintDialogs() {
    }

    /**
     * Shows a simple alert dialog with a single dismiss action.
     *
     * @param title dialog title
     * @param text  dialog body text
     */
    public void alert(String title, String text) {
        Log.d(TAG, "alert: Not implemented yet.");
    }

    /**
     * Shows a confirm dialog with accept/cancel actions.
     *
     * @param title    dialog title
     * @param text     dialog body text
     * @param callback invoked with the user's choice
     */
    public void confirm(String title, String text, CaintCallback callback) {
        Log.d(TAG, "confirm: Not implemented yet.");
    }
}
