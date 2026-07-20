package com.caint.api;

import android.util.Log;

/**
 * Caint.Settings
 *
 * Key/value settings surface for Caint and, eventually, plugins. Backing
 * storage is not implemented yet -- this only establishes the public shape.
 *
 * All methods are placeholders for now.
 */
public final class CaintSettings {

    private static final String TAG = "Caint.Settings";

    CaintSettings() {
    }

    /**
     * Retrieves a stored value for the given key.
     *
     * @param key the settings key
     * @return the stored value, or null (placeholder)
     */
    public Object get(String key) {
        Log.d(TAG, "get: Not implemented yet.");
        return null;
    }

    /**
     * Stores a value for the given key.
     *
     * @param key   the settings key
     * @param value the value to store
     */
    public void set(String key, Object value) {
        Log.d(TAG, "set: Not implemented yet.");
    }
}
