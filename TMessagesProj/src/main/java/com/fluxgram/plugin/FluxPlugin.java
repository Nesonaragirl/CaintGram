package com.fluxgram.plugin;

import org.luaj.vm2.Globals;

/**
 * A single loaded plugin: its manifest, script file, and its own,
 * unshared Lua state.
 *
 * Deliberately still just a data holder -- lifecycle hooks
 * (onEnable/onDisable) and per-plugin component tracking are follow-up
 * work once the loading mechanism itself is solid.
 */
public final class FluxPlugin {

    public final String id;
    public final String fileName;
    public final PluginManifest manifest;

    final Globals globals;

    FluxPlugin(String id, String fileName, PluginManifest manifest, Globals globals) {
        this.id = id;
        this.fileName = fileName;
        this.manifest = manifest;
        this.globals = globals;
    }
}
