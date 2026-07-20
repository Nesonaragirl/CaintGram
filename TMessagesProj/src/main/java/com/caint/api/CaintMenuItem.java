package com.caint.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.Theme;

/**
 * CaintMenuItem
 *
 * Backs Caint.UI.createMenuItem(...). Wraps Telegram's own
 * ActionBarMenuSubItem -- the same row component Telegram uses for its
 * native popup/context menus -- without exposing that class to callers.
 * ActionBarMenuSubItem already styles itself from Telegram's theme colors
 * out of the box, so this looks fully native by default. A plugin can call
 * setTextColor() (applied to both the label and icon) and
 * setBackgroundColor() (applied to the row's press/selector highlight,
 * since a menu row has no filled background of its own) to opt into fixed
 * literal colors instead; passing null to either reverts that part back to
 * Telegram's themed default.
 *
 * Implements CaintViewProvider so it can also be placed inside a
 * CaintContainer (Caint.UI.ChatMenu, Caint.UI.MessageMenu, etc), in addition
 * to its own attach().
 */
final class CaintMenuItem implements CaintComponent, CaintViewProvider {

    private static final String TAG = "Caint.UI.MenuItem";

    private final ActionBarMenuSubItem view;

    // null means "use Telegram's own themed default"; non-null means a
    // plugin explicitly overrode it via setBackgroundColor()/setTextColor().
    private Integer backgroundColorOverride;
    private Integer textColorOverride;

    CaintMenuItem(Context context, String text, int iconResId) {
        view = new ActionBarMenuSubItem(context, true, true);
        view.setTextAndIcon(text != null ? text : "", iconResId);
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public CaintComponent setText(String text) {
        view.setText(text != null ? text : "");
        return this;
    }

    @Override
    public CaintComponent setIcon(int iconResId) {
        view.setIcon(iconResId);
        return this;
    }

    @Override
    public CaintComponent setBackgroundColor(Integer color) {
        backgroundColorOverride = color;
        view.setSelectorColor(color != null ? color : Theme.getColor(Theme.key_dialogButtonSelector));
        return this;
    }

    @Override
    public CaintComponent setTextColor(Integer color) {
        textColorOverride = color;
        int resolved = color != null ? color : Theme.getColor(Theme.key_actionBarDefaultSubmenuItem);
        int resolvedIcon = color != null ? color : Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon);
        view.setColors(resolved, resolvedIcon);
        return this;
    }

    @Override
    public CaintComponent setVisible(boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    @Override
    public CaintComponent setEnabled(boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.5f);
        return this;
    }

    @Override
    public CaintComponent onClick(CaintCallback callback) {
        view.setOnClickListener(v -> {
            if (callback != null) {
                callback.run(null);
            }
        });
        return this;
    }

    @Override
    public CaintComponent attach() {
        // A plugin can call this before any Activity has resumed (plugins
        // load during Application.postInitApplication()). Retry once one
        // becomes available instead of silently dropping the attach.
        CaintActivityTracker.runWhenActivityAvailable(this::doAttach);
        return this;
    }

    private void doAttach() {
        Activity activity = CaintActivityTracker.getCurrentActivity();
        if (activity == null) {
            Log.d(TAG, "attach: no foreground Activity to attach to.");
            return;
        }
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            Log.d(TAG, "attach: current Activity has no content root.");
            return;
        }
        ViewGroup currentParent = (ViewGroup) view.getParent();
        if (currentParent != null) {
            currentParent.removeView(view);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.leftMargin = AndroidUtilities.dp(16);
        params.bottomMargin = AndroidUtilities.dp(16);
        content.addView(view, params);
    }

    @Override
    public void remove() {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
    }
}
