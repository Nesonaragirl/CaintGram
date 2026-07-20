package com.caint.api;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shared bookkeeping and placement logic for every Caint.UI container. See
 * {@link CaintContainer} for the bound-vs-unbound placement story.
 */
class AbstractCaintContainer implements CaintContainer {

    private static final String TAG = "Caint.UI.Container";

    private final ContainerType type;
    private final int defaultGravity;
    private final Map<String, CaintComponent> components = new LinkedHashMap<>();

    private ViewGroup host;

    AbstractCaintContainer(ContainerType type, int defaultGravity) {
        this.type = type;
        this.defaultGravity = defaultGravity;
    }

    @Override
    public ContainerType getType() {
        return type;
    }

    @Override
    public void bindHost(ViewGroup hostView) {
        host = hostView;
        for (CaintComponent component : components.values()) {
            place(component);
        }
    }

    @Override
    public void unbindHost() {
        host = null;
        for (CaintComponent component : components.values()) {
            place(component);
        }
    }

    @Override
    public String add(CaintComponent component) {
        return add(UUID.randomUUID().toString(), component);
    }

    @Override
    public String add(String id, CaintComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("component == null");
        }
        remove(id);
        components.put(id, component);
        place(component);
        return id;
    }

    @Override
    public void remove(CaintComponent component) {
        String matchingId = null;
        for (Map.Entry<String, CaintComponent> entry : components.entrySet()) {
            if (entry.getValue() == component) {
                matchingId = entry.getKey();
                break;
            }
        }
        remove(matchingId);
    }

    @Override
    public void remove(String id) {
        if (id == null) {
            return;
        }
        CaintComponent removed = components.remove(id);
        if (removed != null) {
            removed.remove();
        }
    }

    @Override
    public void clear() {
        List<CaintComponent> snapshot = new ArrayList<>(components.values());
        components.clear();
        for (CaintComponent component : snapshot) {
            component.remove();
        }
    }

    @Override
    public CaintComponent get(String id) {
        return id == null ? null : components.get(id);
    }

    @Override
    public List<CaintComponent> getAll() {
        return new ArrayList<>(components.values());
    }

    @Override
    public int size() {
        return components.size();
    }

    private void place(CaintComponent component) {
        if (!(component instanceof CaintViewProvider)) {
            // Non-view components (e.g. a dialog) have no location to be placed in.
            return;
        }
        View view = ((CaintViewProvider) component).getView();

        ViewGroup target = host != null ? host : fallbackHost();
        if (target == null) {
            // Plugins can add() to a container before any Activity has
            // resumed (they load during Application.postInitApplication()).
            // Retry once one becomes available instead of dropping this
            // placement forever. If the component or container changes
            // state before then, this re-checks host/membership at retry
            // time rather than blindly re-placing.
            Log.d(TAG, "place: no host available for " + type + " yet, will retry.");
            CaintActivityTracker.runWhenActivityAvailable(() -> {
                if (components.get(idOf(component)) == component) {
                    place(component);
                }
            });
            return;
        }

        ViewGroup currentParent = (ViewGroup) view.getParent();
        if (currentParent != null) {
            currentParent.removeView(view);
        }

        if (host != null) {
            // Bound to a real native location -- let it lay the view out however it wants.
            host.addView(view);
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = defaultGravity;
            params.leftMargin = params.rightMargin = params.topMargin = params.bottomMargin = AndroidUtilities.dp(16);
            target.addView(view, params);
        }
    }

    private String idOf(CaintComponent component) {
        for (Map.Entry<String, CaintComponent> entry : components.entrySet()) {
            if (entry.getValue() == component) {
                return entry.getKey();
            }
        }
        return null;
    }

    private ViewGroup fallbackHost() {
        Activity activity = CaintActivityTracker.getCurrentActivity();
        if (activity == null) {
            return null;
        }
        return activity.findViewById(android.R.id.content);
    }
}
