package com.fluxgram.api;

import android.util.Log;

/**
 * Flux.Events
 *
 * Simple pub/sub surface for reacting to and broadcasting Flux events.
 * Intentionally storage-free for now -- subscription bookkeeping will be
 * added once the event system is actually implemented.
 *
 * All methods are placeholders for now.
 */
public final class FluxEvents {

    private static final String TAG = "Flux.Events";

    FluxEvents() {
    }

    /**
     * Registers a callback for a named event.
     *
     * @param event    the event name to listen for
     * @param callback invoked when the event is emitted
     */
    public void on(String event, FluxCallback callback) {
        Log.d(TAG, "on: Not implemented yet.");
    }

    /**
     * Emits a named event to any registered listeners.
     *
     * @param event the event name to emit
     * @param data  payload passed to listeners
     */
    public void emit(String event, Object data) {
        Log.d(TAG, "emit: Not implemented yet.");
    }
}
