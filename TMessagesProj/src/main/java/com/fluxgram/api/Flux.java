package com.fluxgram.api;

/**
 * Flux
 *
 * Public entry point for the Flux API wrapper.
 *
 * Flux exposes a stable, modular surface for interacting with the client
 * without leaking Telegram's internal classes to consumers. Each module
 * below (UI, Events, Settings, Dialogs, Utils) is a self-contained namespace
 * that can evolve independently.
 *
 * This is the foundation layer only: no plugin system, no sandboxing, and
 * no real functionality yet. All module methods are placeholders that will
 * be filled in over time without changing this public API.
 *
 * Usage:
 *   Flux.UI.createButton(...);
 *   Flux.Events.on("someEvent", callback);
 *   Flux.Settings.get("key");
 *   Flux.Dialogs.alert("Title", "Text");
 */
public final class Flux {

    public static final FluxUI UI = new FluxUI();
    public static final FluxEvents Events = new FluxEvents();
    public static final FluxSettings Settings = new FluxSettings();
    public static final FluxDialogs Dialogs = new FluxDialogs();
    public static final FluxUtils Utils = new FluxUtils();

    private Flux() {
        // Flux is a static entry point and is never instantiated.
    }
}
