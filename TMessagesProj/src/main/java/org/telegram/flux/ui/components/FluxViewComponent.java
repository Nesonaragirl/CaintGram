package org.telegram.flux.ui.components;

import android.view.View;

import org.telegram.flux.ui.FluxComponent;

/**
 * A component that wraps a plain Android {@link View} supplied by caller code
 * (a plugin's own custom view, never a Telegram-internal view class). Used by
 * the view-based containers: ChatHeader, ChatFooter, Sidebar, BottomBar.
 */
public class FluxViewComponent extends FluxComponent {

    private final View view;
    private int order;

    public FluxViewComponent(String id, View view) {
        super(id);
        if (view == null) {
            throw new IllegalArgumentException("view == null");
        }
        this.view = view;
    }

    public FluxViewComponent(View view) {
        this(null, view);
    }

    public View getView() {
        return view;
    }

    /** Optional ordering hint used by the host when several components share a container. Lower = earlier. */
    public int getOrder() {
        return order;
    }

    public FluxViewComponent setOrder(int order) {
        this.order = order;
        return this;
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
