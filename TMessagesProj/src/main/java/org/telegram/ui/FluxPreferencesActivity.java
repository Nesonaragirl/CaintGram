package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;

import androidx.documentfile.provider.DocumentFile;

import com.fluxgram.plugin.FluxPlugin;
import com.fluxgram.plugin.PluginManager;
import com.fluxgram.plugin.PluginManifest;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
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

/**
 * FluxGram Preferences screen, opened from the top row in Settings.
 *
 * Sections:
 *  - Plugins: entry point into the plugin system (see {@link PluginsActivity}).
 *             "My Plugins" and "Import from File" (zip or folder) are wired
 *             up; the rest of the rows in this screen are placeholders for
 *             future functionality.
 *  - Appearance: client customization options (themes, icon, fonts, bubbles, accent color).
 *  - Updates & About: version/update info and links.
 */
public class FluxPreferencesActivity extends BaseFragment {

    private static final int ID_MY_PLUGINS = 1;
    private static final int ID_AUTO_UPDATE_PLUGINS = 3;
    private static final int ID_IMPORT_FROM_FILE = 4;

    private static final int ID_CUSTOM_THEMES = 5;
    private static final int ID_APP_ICON = 6;
    private static final int ID_CUSTOM_FONTS = 7;
    private static final int ID_BUBBLE_STYLE = 8;
    private static final int ID_ACCENT_COLOR = 9;

    private static final int ID_CHECK_UPDATES = 10;
    private static final int ID_CHANGELOG = 11;
    private static final int ID_GITHUB_REPOSITORY = 12;
    private static final int ID_ABOUT_FLUXGRAM = 13;

    private static final int REQUEST_CODE_IMPORT_ZIP = 4001;
    private static final int REQUEST_CODE_IMPORT_FOLDER = 4002;

    private boolean autoUpdatePlugins = true;
    private boolean customFonts = false;

    private UniversalRecyclerView listView;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("FluxGram Preferences");
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

        listView = new UniversalRecyclerView(this, this::fillItems, this::onClick, null);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        // Plugins
        items.add(UItem.asHeader("Plugins"));
        items.add(UItem.asButton(ID_MY_PLUGINS, R.drawable.settings_features, "My Plugins", "3"));
        items.add(UItem.asSwitch(ID_AUTO_UPDATE_PLUGINS, "Auto-Update Plugins").setChecked(autoUpdatePlugins));
        items.add(UItem.asButton(ID_IMPORT_FROM_FILE, "Import from File", ""));
        items.add(UItem.asShadow(null));

        // Appearance
        items.add(UItem.asHeader("Appearance"));
        items.add(UItem.asButton(ID_CUSTOM_THEMES, "Custom Themes", "2"));
        items.add(UItem.asButton(ID_APP_ICON, "App Icon", "Default"));
        items.add(UItem.asSwitch(ID_CUSTOM_FONTS, "Custom Fonts").setChecked(customFonts));
        items.add(UItem.asButton(ID_BUBBLE_STYLE, "Bubble Style", "Rounded"));
        items.add(UItem.asButton(ID_ACCENT_COLOR, "Accent Color", ""));
        items.add(UItem.asShadow("Manage the appearance, plugins, and updates of your FluxGram client."));

        // Updates & About
        items.add(UItem.asHeader("Updates & About"));
        items.add(UItem.asButton(ID_CHECK_UPDATES, "Check for Updates", "v12.8.1"));
        items.add(UItem.asButton(ID_CHANGELOG, "Changelog"));
        items.add(UItem.asButton(ID_GITHUB_REPOSITORY, "GitHub Repository"));
        items.add(UItem.asButton(ID_ABOUT_FLUXGRAM, "About FluxGram"));
        items.add(UItem.asShadow(null));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ID_MY_PLUGINS) {
            presentFragment(new PluginsActivity());
        } else if (item.id == ID_AUTO_UPDATE_PLUGINS) {
            autoUpdatePlugins = !autoUpdatePlugins;
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        } else if (item.id == ID_IMPORT_FROM_FILE) {
            showImportSourcePicker();
        } else if (item.id == ID_CUSTOM_FONTS) {
            customFonts = !customFonts;
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        }
        // All other rows are placeholders for now and intentionally have no action.
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
            FluxPlugin plugin = PluginManager.importFromZip(tempZip);
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
            FluxPlugin plugin = PluginManager.importFromFolder(tempFolder);
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

    private void showImportSuccess(FluxPlugin plugin) {
        if (getContext() == null) {
            return;
        }
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
