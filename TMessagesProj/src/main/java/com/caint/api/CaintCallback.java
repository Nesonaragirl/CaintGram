package com.caint.api;

/**
 * Generic callback interface used across Caint modules (Events, Dialogs, UI
 * click handlers, etc). Kept intentionally simple during the foundation
 * phase so it can be reshaped once real functionality lands.
 */
public interface CaintCallback {
    void run(Object data);
}
