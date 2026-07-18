package com.fluxgram.api;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;

/**
 * Bridges a curated set of Telegram internal events (from NotificationCenter)
 * onto the Flux event bus, translating them into plain, Flux-safe payloads.
 *
 * This is the ONLY place in the wrapper that is allowed to touch
 * NotificationCenter directly. Everything downstream of it (Flux.Events
 * listeners) only ever sees primitive data -- never TLRPC objects or other
 * Telegram internals -- which is what keeps Flux.Events safe to expose to
 * future plugin code.
 *
 * To bridge an additional Telegram event, add its id to BRIDGED_IDS and a
 * case to mapEventName -- nothing else needs to change.
 */
final class FluxNotificationBridge implements NotificationCenter.NotificationCenterDelegate {

    private static final FluxNotificationBridge INSTANCE = new FluxNotificationBridge();
    private static boolean attached;

    // Curated subset of NotificationCenter ids that are currently bridged.
    // Kept small and deliberate rather than mirroring all ~300 Telegram
    // notifications -- more can be added over time as real use cases show up.
    private static final int[] BRIDGED_IDS = {
            NotificationCenter.didReceiveNewMessages,
            NotificationCenter.messagesDidLoad,
            NotificationCenter.messagesDeleted,
            NotificationCenter.dialogsNeedReload,
            NotificationCenter.didUpdateConnectionState,
            NotificationCenter.appDidLogout,
    };

    private FluxNotificationBridge() {
    }

    /**
     * Registers this bridge as an observer on every account's
     * NotificationCenter. Safe to call more than once -- only attaches once.
     * Must be called from the UI thread (NotificationCenter requirement).
     */
    static void attach() {
        if (attached) {
            return;
        }
        attached = true;

        for (int account = 0; account < UserConfig.MAX_ACCOUNT_COUNT; account++) {
            NotificationCenter center = NotificationCenter.getInstance(account);
            for (int id : BRIDGED_IDS) {
                center.addObserver(INSTANCE, id);
            }
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        String eventName = mapEventName(id);
        if (eventName == null) {
            return;
        }
        // Intentionally emit only primitive, Flux-safe data (the account
        // index) -- never the raw Telegram objects carried in `args`.
        Flux.Events.emit(eventName, account);
    }

    private static String mapEventName(int id) {
        if (id == NotificationCenter.didReceiveNewMessages) {
            return "messagesReceived";
        } else if (id == NotificationCenter.messagesDidLoad) {
            return "messagesLoaded";
        } else if (id == NotificationCenter.messagesDeleted) {
            return "messagesDeleted";
        } else if (id == NotificationCenter.dialogsNeedReload) {
            return "dialogsReload";
        } else if (id == NotificationCenter.didUpdateConnectionState) {
            return "connectionStateChanged";
        } else if (id == NotificationCenter.appDidLogout) {
            return "userLoggedOut";
        }
        return null;
    }
}
