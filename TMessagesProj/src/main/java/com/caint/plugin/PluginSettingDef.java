package com.caint.plugin;

/** One setting a plugin declared via Caint.Settings.register(key, type, label, default) in its Lua script. */
public final class PluginSettingDef {

    public enum Type { TOGGLE, TEXT }

    public final String key;
    public final Type type;
    public final String label;
    public final String defaultValue;

    public PluginSettingDef(String key, Type type, String label, String defaultValue) {
        this.key = key;
        this.type = type;
        this.label = label;
        this.defaultValue = defaultValue;
    }
}
