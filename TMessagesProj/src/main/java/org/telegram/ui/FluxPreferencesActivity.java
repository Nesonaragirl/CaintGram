package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * FluxGram Preferences screen, opened from the top row in Settings.
 *
 * Sections:
 *  - Plugins: entry point into the plugin system. "My Plugins" opens
 *             {@link PluginsActivity}, which now also owns importing
 *             plugins from a file; Auto-Update Plugins is a placeholder
 *             toggle.
 *  - Updates & About: current version, changelog, GitHub repository, and about links.
 *
 * Rows use SettingsActivity.SettingCell - the same gradient-icon card used for the
 * "FluxGram Preferences" entry row (and every other section) on the main Settings screen -
 * so this feels like a native part of the app rather than a bolted-on plugin panel.
 */
public class FluxPreferencesActivity extends BaseFragment {

    private static final String FLUXGRAM_VERSION = "0.1.0 - dev";
    private static final String GITHUB_REPOSITORY_URL = "https://github.com/Nesonaragirl/Fluxgram";
    private static final String CHANGELOG_ASSET = "changelogs.md";

    private static final int ID_MY_PLUGINS = 1;
    private static final int ID_AUTO_UPDATE_PLUGINS = 3;

    private static final int ID_VERSIONS = 10;
    private static final int ID_CHANGELOG = 11;
    private static final int ID_GITHUB_REPOSITORY = 12;
    private static final int ID_ABOUT_FLUXGRAM = 13;

    private boolean autoUpdatePlugins = true;

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
        items.add(SettingsActivity.SettingCell.Factory.of(ID_MY_PLUGINS, IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom, R.drawable.settings_features, "My Plugins", null, String.valueOf(com.caint.plugin.PluginManager.getLoadedPlugins().size())));
        items.add(UItem.asSwitch(ID_AUTO_UPDATE_PLUGINS, "Auto-Update Plugins").setChecked(autoUpdatePlugins));
        items.add(UItem.asShadow("Import, enable, and configure plugins from My Plugins."));

        // Updates & About
        items.add(UItem.asHeader("Updates & About"));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_VERSIONS, IconBackgroundColors.CYAN.top, IconBackgroundColors.CYAN.bottom, R.drawable.settings_devices, "Versions", null, FLUXGRAM_VERSION));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_CHANGELOG, IconBackgroundColors.BLUE_LIGHT.top, IconBackgroundColors.BLUE_LIGHT.bottom, R.drawable.settings_faq, "Changelog"));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_GITHUB_REPOSITORY, IconBackgroundColors.GREEN.top, IconBackgroundColors.GREEN.bottom, R.drawable.msg_link2, "GitHub Repository"));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_ABOUT_FLUXGRAM, IconBackgroundColors.ORANGE.top, IconBackgroundColors.ORANGE.bottom, R.drawable.settings_policy, "About FluxGram"));
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
        } else if (item.id == ID_CHANGELOG) {
            showChangelogDialog();
        } else if (item.id == ID_GITHUB_REPOSITORY) {
            openUrl(GITHUB_REPOSITORY_URL);
        } else if (item.id == ID_ABOUT_FLUXGRAM) {
            showAboutDialog();
        }
    }

    /**
     * Shows the current version and recent log entries in an in-app popup.
     * The text is read from the bundled {@code changelogs.md} asset, so this
     * never has to reach out to the GitHub repository or its commit history.
     */
    private void showChangelogDialog() {
        if (getContext() == null) {
            return;
        }
        String message = FLUXGRAM_VERSION + "\n\n" + loadChangelogText();
        new AlertDialog.Builder(getContext(), getResourceProvider())
                .setTitle("Changelogs")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /** Reads changelogs.md from assets and turns its lightweight markdown into plain text for the popup. */
    private String loadChangelogText() {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = ApplicationLoader.applicationContext.getAssets().open(CHANGELOG_ASSET);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                String formatted = formatChangelogLine(line);
                if (formatted == null) {
                    continue;
                }
                if (!first) {
                    sb.append('\n');
                }
                sb.append(formatted);
                first = false;
            }
        } catch (IOException e) {
            return "Couldn't load the changelog.";
        }
        return sb.toString();
    }

    /** Filters a single markdown line down to plain text suitable for an AlertDialog message. */
    private String formatChangelogLine(String line) {
        if (line.isEmpty()) {
            return "";
        }
        if (line.startsWith("## ")) {
            return "\n" + line.substring(3);
        }
        if (line.startsWith("# ")) {
            return null;
        }
        if (line.startsWith("- ")) {
            return "\u2022 " + line.substring(2);
        }
        return line;
    }

    private void openUrl(String url) {
        if (getContext() == null) {
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getContext().startActivity(intent);
        } catch (Exception e) {
            // No browser available on this device; nothing more we can do here.
        }
    }

    private void showAboutDialog() {
        if (getContext() == null) {
            return;
        }
        new AlertDialog.Builder(getContext(), getResourceProvider())
                .setTitle("About FluxGram")
                .setMessage("FluxGram " + FLUXGRAM_VERSION + "\n\nA Telegram client fork with a Lua-based plugin system, built on top of DrKLO/Telegram.\n\n" + GITHUB_REPOSITORY_URL)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }
}
