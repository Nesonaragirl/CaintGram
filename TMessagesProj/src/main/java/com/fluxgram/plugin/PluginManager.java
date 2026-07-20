package com.fluxgram.plugin;

import android.util.Log;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.telegram.messenger.ApplicationLoader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Discovers and loads plugins from disk, using each plugin's manifest.json
 * to find its Entrypoint script.
 *
 * Expected layout, one subfolder per plugin under the plugins root:
 *
 *   <pluginsRoot>/<pluginFolder>/manifest.json
 *   <pluginsRoot>/<pluginFolder>/<Entrypoint from manifest, e.g. main.lua>
 *   <pluginsRoot>/<pluginFolder>/...any other files the entrypoint needs
 *
 * A malformed manifest, a missing entrypoint file, or a plugin script that
 * throws while loading only skips that one plugin -- it never stops the
 * rest of the scan or crashes the host app.
 */
public final class PluginManager {

    private static final String TAG = "Flux.PluginManager";
    private static final String PLUGINS_DIR_NAME = "flux_plugins";
    private static final String MANIFEST_FILE_NAME = "manifest.json";

    private static final List<FluxPlugin> loadedPlugins = new ArrayList<>();

    private PluginManager() {
    }

    /**
     * The root directory plugins are discovered from -- one subfolder per
     * plugin, created under the app's private files directory (no storage
     * permission required).
     */
    public static File getPluginsRoot() {
        File root = new File(ApplicationLoader.applicationContext.getFilesDir(), PLUGINS_DIR_NAME);
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }

    /** The plugins currently loaded in memory. Empty until loadAll() has run at least once. */
    public static List<FluxPlugin> getLoadedPlugins() {
        return Collections.unmodifiableList(loadedPlugins);
    }

    /**
     * Scans the plugins root directory and (re)loads every valid plugin
     * found there. Clears anything previously loaded first.
     *
     * @return the plugins that loaded successfully
     */
    public static List<FluxPlugin> loadAll() {
        loadedPlugins.clear();

        File root = getPluginsRoot();
        File[] pluginDirs = root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        if (pluginDirs == null) {
            return getLoadedPlugins();
        }

        for (File pluginDir : pluginDirs) {
            FluxPlugin plugin = loadPlugin(pluginDir);
            if (plugin != null) {
                loadedPlugins.add(plugin);
            }
        }

        return getLoadedPlugins();
    }

    /**
     * Loads a single plugin from its folder. Returns null (and logs why)
     * if the manifest is missing/invalid, the entrypoint file is missing,
     * or the script itself throws while executing.
     */
    private static FluxPlugin loadPlugin(File pluginDir) {
        String id = pluginDir.getName();

        File manifestFile = new File(pluginDir, MANIFEST_FILE_NAME);
        if (!manifestFile.exists()) {
            Log.w(TAG, "Skipping \"" + id + "\": no manifest.json found");
            return null;
        }

        String manifestJson;
        try {
            manifestJson = readFile(manifestFile);
        } catch (IOException e) {
            Log.w(TAG, "Skipping \"" + id + "\": failed to read manifest.json", e);
            return null;
        }

        PluginManifest manifest;
        try {
            manifest = PluginManifest.parse(manifestJson);
        } catch (PluginManifest.ManifestException e) {
            Log.w(TAG, "Skipping \"" + id + "\": " + e.getMessage());
            return null;
        }

        File entrypointFile = new File(pluginDir, manifest.entrypoint);
        if (!entrypointFile.exists()) {
            Log.w(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): entrypoint \""
                    + manifest.entrypoint + "\" not found");
            return null;
        }

        String script;
        try {
            script = readFile(entrypointFile);
        } catch (IOException e) {
            Log.w(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): failed to read entrypoint", e);
            return null;
        }

        Globals globals = JsePlatform.standardGlobals();
        LuaTable fluxTable = new FluxLuaBridge().asLuaTable();
        globals.set("Flux", fluxTable);

        try {
            LuaValue chunk = globals.load(script, manifest.entrypoint);
            chunk.call();
        } catch (LuaError e) {
            Log.e(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): script threw while loading", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Skipping \"" + id + "\" (" + manifest.name + "): unexpected error while loading", e);
            return null;
        }

        Log.i(TAG, "Loaded plugin \"" + manifest.name + "\" v" + manifest.version + " (" + id + ")");
        return new FluxPlugin(id, manifest.entrypoint, manifest, globals);
    }

    private static String readFile(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        InputStream in = new FileInputStream(file);
        try {
            int offset = 0;
            int read;
            while (offset < bytes.length && (read = in.read(bytes, offset, bytes.length - offset)) != -1) {
                offset += read;
            }
        } finally {
            in.close();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
