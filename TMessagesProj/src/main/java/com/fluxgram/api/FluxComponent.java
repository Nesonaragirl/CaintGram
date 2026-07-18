package com.fluxgram.api;

/**
 * Common surface implemented by every UI element Flux.UI creates (buttons,
 * menu items, dialogs, and future component types). This is what keeps
 * Flux.UI's output uniform regardless of which native Telegram view backs a
 * given component underneath.
 *
 * Implementations wrap a real Android/Telegram view internally but never
 * expose it through this interface -- callers only ever see FluxComponent.
 */
public interface FluxComponent {

    /**
     * Updates the component's primary text (button label, menu item text,
     * dialog message, etc).
     */
    FluxComponent setText(String text);

    /**
     * Sets the component's icon from an Android drawable resource id.
     */
    FluxComponent setIcon(int iconResId);

    /**
     * Shows or hides the component.
     */
    FluxComponent setVisible(boolean visible);

    /**
     * Enables or disables interaction with the component.
     */
    FluxComponent setEnabled(boolean enabled);

    /**
     * Registers a callback fired when the component is activated (tapped,
     * confirmed, etc, depending on the component type).
     */
    FluxComponent onClick(FluxCallback callback);

    /**
     * Attaches the component to the current foreground screen so it's
     * actually visible, using whatever placement makes sense for its type
     * (e.g. a corner overlay for buttons/menu items). Dialogs are already
     * shown as soon as they're created, so this is a no-op for them.
     *
     * No-ops (and logs) if there's currently no foreground Activity to
     * attach to.
     */
    FluxComponent attach();

    /**
     * Permanently removes the component from wherever it's attached
     * (detaches its view from its parent, dismisses it if it's a dialog).
     */
    void remove();
}
