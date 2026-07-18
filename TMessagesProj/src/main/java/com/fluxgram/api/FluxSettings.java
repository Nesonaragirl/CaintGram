package com.fluxgram.api;

import android.util.Log;

/**
 * Flux.Settings
 *
 * Key/value settings surface for Flux and, eventually, plugins. Backing
 * storage is not implemented yet -- this only establishes the public shape.
 *
 * All methods are placeholders for now.
 */
public final class FluxSettings {

    private static final String TAG = "Flux.Settings";

    FluxSettings() {
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
