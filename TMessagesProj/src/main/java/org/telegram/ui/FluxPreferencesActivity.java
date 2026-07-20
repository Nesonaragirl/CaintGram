package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

/**
 * FluxGram Preferences screen, opened from the top row in Settings.
 *
 * Sections:
 *  - Plugins: entry point into the plugin system (see {@link PluginsActivity}).
 *             Only "My Plugins" is wired up right now; the rest of the rows in
 *             this screen are placeholders for future functionality.
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
        } else if (item.id == ID_CUSTOM_FONTS) {
            customFonts = !customFonts;
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        }
        // All other rows are placeholders for now and intentionally have no action.
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }
}
