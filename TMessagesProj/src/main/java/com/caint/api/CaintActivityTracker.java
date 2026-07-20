package com.caint.api;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the current foreground Activity so Caint.UI components can attach
 * themselves somewhere real without Caint needing to know anything about
 * Telegram's fragment/navigation internals.
 *
 * This is intentionally the only thing Caint knows about Activities -- a
 * weak reference to whichever one is currently resumed.
 *
 * Plugins load (and run their top-level code, e.g. component:attach())
 * from Caint.init(), which happens during Application.postInitApplication()
 * -- before any Activity has resumed yet. A component that tries to attach
 * at that point would otherwise find getCurrentActivity() == null and just
 * silently give up forever. runWhenActivityAvailable() exists so that case
 * retries automatically the moment an Activity actually becomes available,
 * instead of requiring the caller to know about this ordering at all.
 */
final class CaintActivityTracker implements Application.ActivityLifecycleCallbacks {

    private static final CaintActivityTracker INSTANCE = new CaintActivityTracker();
    private static boolean attached;

    private static final List<Runnable> pendingActions = new ArrayList<>();

    private WeakReference<Activity> currentActivity = new WeakReference<>(null);

    private CaintActivityTracker() {
    }

    static void attach(Application application) {
        if (attached) {
            return;
        }
        attached = true;
        application.registerActivityLifecycleCallbacks(INSTANCE);
    }

    static Activity getCurrentActivity() {
        return INSTANCE.currentActivity.get();
    }

    /**
     * Runs {@code action} immediately if an Activity is currently available,
     * otherwise queues it to run the moment the next Activity resumes.
     * Safe to call from any thread that itself only touches views on the
     * main thread inside {@code action} (same contract as the rest of Caint.UI).
     */
    static void runWhenActivityAvailable(Runnable action) {
        if (getCurrentActivity() != null) {
            action.run();
        } else {
            pendingActions.add(action);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = new WeakReference<>(activity);
        if (!pendingActions.isEmpty()) {
            List<Runnable> toRun = new ArrayList<>(pendingActions);
            pendingActions.clear();
            for (Runnable action : toRun) {
                action.run();
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (currentActivity.get() == activity) {
            currentActivity.clear();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
