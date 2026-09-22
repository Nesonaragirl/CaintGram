package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;

import androidx.documentfile.provider.DocumentFile;

import com.caint.plugin.CaintPlugin;
import com.caint.plugin.PluginManager;
import com.caint.plugin.PluginManifest;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modern "My Plugins" screen.
 *
 * Loaded plugins are split into two headered groups -- Enabled first, then
 * Disabled -- each rendered as an expandable row (name + description, with
 * a switch for enabling/disabling and a collapse arrow). Tapping the arrow
 * rolls out that plugin's own on/off settings, as declared in its
 * manifest.json's "Settings" array (see PluginManifest.PluginSetting) --
 * fully plugin-defined, no two plugins need show the same options.
 *
 * "Import from File" (zip or unpacked folder) lives here now, next to the
 * plugins it populates, instead of on the FluxGram Preferences screen.
 */
public class PluginsActivity extends BaseFragment {

    private static final int ID_IMPORT_FROM_FILE = 1;
    private static final int ID_GET_NEW_PLUGINS = 2;
    // Stable per-plugin id ranges: up to 100 settings per plugin.
    private static final int ID_PLUGIN_BASE = 1000;
    private static final int ID_PLUGIN_SETTING_BASE = 100000;
    private static final int SETTINGS_PER_PLUGIN = 100;

    private static final int REQUEST_CODE_IMPORT_ZIP = 4001;
    private static final int REQUEST_CODE_IMPORT_FOLDER = 4002;

    private UniversalRecyclerView listView;
    private final Set<String> expandedPluginIds = new HashSet<>();

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("My Plugins");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        final FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, this::onLongClick);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        List<CaintPlugin> plugins = PluginManager.getLoadedPlugins();
        List<CaintPlugin> enabled = new ArrayList<>();
        List<CaintPlugin> disabled = new ArrayList<>();
        for (CaintPlugin plugin : plugins) {
            (PluginManager.isPluginVisible(plugin.id) ? enabled : disabled).add(plugin);
        }

        if (plugins.isEmpty()) {
            items.add(UItem.asHeader("My plugins"));
            items.add(UItem.asShadow("No plugins installed yet. Import one from a file or get new ones below."));
        } else {
            items.add(UItem.asHeader("Enabled"));
            if (enabled.isEmpty()) {
                items.add(UItem.asShadow("No enabled plugins."));
            } else {
                for (CaintPlugin plugin : enabled) {
                    addPluginRows(items, plugin, plugins.indexOf(plugin), true);
                }
            }

            if (!disabled.isEmpty()) {
                items.add(UItem.asHeader("Disabled"));
                for (CaintPlugin plugin : disabled) {
                    addPluginRows(items, plugin, plugins.indexOf(plugin), false);
                }
            }
            items.add(UItem.asShadow("Tap a plugin to see its settings. Turn it off to disable it without uninstalling."));
        }

        items.add(UItem.asButton(ID_IMPORT_FROM_FILE, R.drawable.settings_data, "Import from File"));
        items.add(UItem.asButton(ID_GET_NEW_PLUGINS, R.drawable.msg2_trending, "Get new plugins").accent());
        items.add(UItem.asShadow(null));
    }

    private void addPluginRows(ArrayList<UItem> items, CaintPlugin plugin, int index, boolean enabled) {
        boolean expanded = expandedPluginIds.contains(plugin.id);
        String subtitle = plugin.manifest.description == null || plugin.manifest.description.isEmpty()
                ? "v" + plugin.manifest.version
                : plugin.manifest.description;

        int pluginItemId = ID_PLUGIN_BASE + index;
        UItem row = UItem.asExpandableSwitch(pluginItemId, plugin.manifest.name, subtitle)
                .setChecked(enabled)
                .setCollapsed(!expanded);
        row.setClickCallback(v -> {
            if (expanded) {
                expandedPluginIds.remove(plugin.id);
            } else {
                expandedPluginIds.add(plugin.id);
            }
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        });
        items.add(row);

        if (expanded) {
            List<PluginManifest.PluginSetting> settings = plugin.manifest.settings;
            if (settings.isEmpty()) {
                items.add(UItem.asShadow("This plugin has no settings."));
            } else {
                for (int s = 0; s < settings.size() && s < SETTINGS_PER_PLUGIN; s++) {
                    PluginManifest.PluginSetting setting = settings.get(s);
                    boolean value = PluginManager.getPluginSetting(plugin.id, setting.key, setting.defaultValue);
                    items.add(UItem.asCheck(ID_PLUGIN_SETTING_BASE + index * SETTINGS_PER_PLUGIN + s, setting.label).setChecked(value));
                }
            }
        }
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ID_IMPORT_FROM_FILE) {
            showImportSourcePicker();
        } else if (item.id == ID_GET_NEW_PLUGINS) {
            // TODO: open the plugin catalog/browser once it exists.
        } else if (item.id >= ID_PLUGIN_SETTING_BASE) {
            int offset = item.id - ID_PLUGIN_SETTING_BASE;
            int pluginIndex = offset / SETTINGS_PER_PLUGIN;
            int settingIndex = offset % SETTINGS_PER_PLUGIN;
            List<CaintPlugin> plugins = PluginManager.getLoadedPlugins();
            if (pluginIndex >= 0 && pluginIndex < plugins.size()) {
                CaintPlugin plugin = plugins.get(pluginIndex);
                List<PluginManifest.PluginSetting> settings = plugin.manifest.settings;
                if (settingIndex >= 0 && settingIndex < settings.size()) {
                    PluginManifest.PluginSetting setting = settings.get(settingIndex);
                    boolean value = !PluginManager.getPluginSetting(plugin.id, setting.key, setting.defaultValue);
                    PluginManager.setPluginSetting(plugin.id, setting.key, value);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(value);
                    }
                }
            }
        } else if (item.id >= ID_PLUGIN_BASE) {
            int index = item.id - ID_PLUGIN_BASE;
            List<CaintPlugin> plugins = PluginManager.getLoadedPlugins();
            if (index >= 0 && index < plugins.size()) {
                CaintPlugin plugin = plugins.get(index);
                boolean visible = !PluginManager.isPluginVisible(plugin.id);
                PluginManager.setPluginVisible(plugin.id, visible);
                if (listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
            }
        }
    }

    private boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    /** Lets the user choose whether they're importing a .zip archive or an unpacked folder. */
    private void showImportSourcePicker() {
        if (getContext() == null) {
            return;
        }
        new AlertDialog.Builder(getContext(), getResourceProvider())
                .setTitle("Import Plugin")
                .setItems(new CharSequence[]{"From Zip File", "From Folder"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/zip");
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/zip", "application/x-zip-compressed"});
                        try {
                            startActivityForResult(intent, REQUEST_CODE_IMPORT_ZIP);
                        } catch (Exception e) {
                            showImportError("Couldn't open a file picker on this device.");
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        try {
                            startActivityForResult(intent, REQUEST_CODE_IMPORT_FOLDER);
                        } catch (Exception e) {
                            showImportError("Couldn't open a folder picker on this device.");
                        }
                    }
                })
                .show();
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        if (requestCode == REQUEST_CODE_IMPORT_ZIP) {
            importZipFromUri(uri);
        } else if (requestCode == REQUEST_CODE_IMPORT_FOLDER) {
            importFolderFromUri(uri);
        }
    }

    private void importZipFromUri(Uri uri) {
        File tempZip = new File(ApplicationLoader.applicationContext.getCacheDir(), "flux_import_" + System.currentTimeMillis() + ".zip");
        try {
            copyUriToFile(uri, tempZip);
            CaintPlugin plugin = PluginManager.importFromZip(tempZip);
            showImportSuccess(plugin);
        } catch (IOException | PluginManifest.ManifestException e) {
            showImportError(e.getMessage() != null ? e.getMessage() : "Couldn't import that zip file.");
        } finally {
            tempZip.delete();
        }
    }

    private void importFolderFromUri(Uri treeUri) {
        File tempFolder = new File(ApplicationLoader.applicationContext.getCacheDir(), "flux_import_" + System.currentTimeMillis());
        try {
            DocumentFile sourceTree = DocumentFile.fromTreeUri(ApplicationLoader.applicationContext, treeUri);
            if (sourceTree == null || !sourceTree.isDirectory()) {
                showImportError("Couldn't read that folder.");
                return;
            }
            if (!tempFolder.mkdirs()) {
                showImportError("Couldn't prepare that folder for import.");
                return;
            }
            copyDocumentTree(sourceTree, tempFolder);
            CaintPlugin plugin = PluginManager.importFromFolder(tempFolder);
            showImportSuccess(plugin);
        } catch (IOException | PluginManifest.ManifestException e) {
            showImportError(e.getMessage() != null ? e.getMessage() : "Couldn't import that folder.");
        } finally {
            deleteRecursive(tempFolder);
        }
    }

    private void copyUriToFile(Uri uri, File dest) throws IOException {
        InputStream in = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
        if (in == null) {
            throw new IOException("Couldn't open the selected file.");
        }
        try {
            OutputStream out = new FileOutputStream(dest);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private void copyDocumentTree(DocumentFile srcDir, File destDir) throws IOException {
        for (DocumentFile child : srcDir.listFiles()) {
            if (child.getName() == null) {
                continue;
            }
            File dest = new File(destDir, child.getName());
            if (child.isDirectory()) {
                if (!dest.mkdirs()) {
                    throw new IOException("Couldn't create directory: " + dest.getPath());
                }
                copyDocumentTree(child, dest);
            } else {
                copyUriToFile(child.getUri(), dest);
            }
        }
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    private void showImportSuccess(CaintPlugin plugin) {
        if (getContext() == null) {
            return;
        }
        expandedPluginIds.add(plugin.id);
        BulletinFactory.of(this).createSimpleBulletin(R.raw.contact_check, "Imported \"" + plugin.manifest.name + "\"").show();
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    private void showImportError(String message) {
        if (getContext() == null) {
            return;
        }
        BulletinFactory.of(this).createErrorBulletin(message).show();
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }
}
