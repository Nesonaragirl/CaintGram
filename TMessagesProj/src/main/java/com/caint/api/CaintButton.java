package com.caint.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

/**
 * CaintButton
 *
 * Backs Caint.UI.createButton(...). Wraps a plain, real Android TextView
 * styled with Telegram's own Theme colors, so it looks fully native by
 * default -- the same blue "add button" look Telegram itself uses. A
 * plugin can call setBackgroundColor()/setTextColor() to opt into a fixed
 * literal color instead; passing null to either clears the override and
 * reverts that part of the button back to Telegram's themed default.
 *
 * Implements CaintViewProvider so it can also be placed inside a
 * CaintContainer (Caint.UI.ChatHeader, etc), in addition to its own attach().
 */
final class CaintButton implements CaintComponent, CaintViewProvider {

    private static final String TAG = "Caint.UI.Button";

    private final TextView view;

    // null means "use Telegram's own themed default"; non-null means a
    // plugin explicitly overrode it via setBackgroundColor()/setTextColor().
    private Integer backgroundColorOverride;
    private Integer textColorOverride;

    CaintButton(Context context, String text) {
        view = new TextView(context);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        view.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(10), AndroidUtilities.dp(24), AndroidUtilities.dp(10));
        view.setText(text != null ? text : "");
        updateStyle();
    }

    /** Recomputes the button's background drawable and text color from either its overrides or Telegram's current theme. */
    private void updateStyle() {
        int background = backgroundColorOverride != null ? backgroundColorOverride : Theme.getColor(Theme.key_featuredStickers_addButton);
        int backgroundPressed = backgroundColorOverride != null ? backgroundColorOverride : Theme.getColor(Theme.key_featuredStickers_addButtonPressed);
        int text = textColorOverride != null ? textColorOverride : Theme.getColor(Theme.key_featuredStickers_buttonText);
        view.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), background, backgroundPressed));
        view.setTextColor(text);
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
        view.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        view.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        return this;
    }

    @Override
    public CaintComponent setBackgroundColor(Integer color) {
        backgroundColorOverride = color;
        updateStyle();
        return this;
    }

    @Override
    public CaintComponent setTextColor(Integer color) {
        textColorOverride = color;
        updateStyle();
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
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.rightMargin = params.bottomMargin = AndroidUtilities.dp(16);
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
