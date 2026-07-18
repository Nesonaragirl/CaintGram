package com.fluxgram.api;

import android.view.View;

/**
 * Implemented by FluxComponents that are backed by a real Android View
 * (buttons, menu items) so containers can place them into a host ViewGroup.
 *
 * Package-private -- this is an internal wiring detail, never part of the
 * public FluxComponent surface callers see.
 */
interface FluxViewProvider {
    View getView();
}
