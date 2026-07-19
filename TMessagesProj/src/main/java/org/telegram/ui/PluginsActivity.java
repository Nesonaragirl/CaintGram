package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;

import java.util.ArrayList;

/**
 * Plugins screen: lists installed plugins (once plugin discovery is wired up),
 * lets the user browse for new ones, and holds the "Plugins toggle" switch
 * that turns detailed plugin information on/off.
 */
public class PluginsActivity extends BaseFragment {

    private static final int ID_GET_NEW_PLUGINS = 1;
    private static final int ID_PLUGINS_TOGGLE = 2;

    private UniversalRecyclerView listView;
    private boolean pluginsToggleEnabled;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Plugins");
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
        // Installed plugins will be listed here once plugin discovery/loading is wired up.

        items.add(UItem.asHeader("My plugins"));
        items.add(UItem.asButton(ID_GET_NEW_PLUGINS, R.drawable.msg2_trending, "Get new plugins").accent());
        items.add(UItem.asShadow(null));

        items.add(UItem.asCheck(ID_PLUGINS_TOGGLE, "Plugins toggle").setChecked(pluginsToggleEnabled));
        items.add(UItem.asShadow("Show detailed plugin information."));
    }

    private void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ID_PLUGINS_TOGGLE) {
            pluginsToggleEnabled = !pluginsToggleEnabled;
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(pluginsToggleEnabled);
            }
        } else if (item.id == ID_GET_NEW_PLUGINS) {
            // TODO: open the plugin catalog/browser once it exists.
        }
    }

    private boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }
}
