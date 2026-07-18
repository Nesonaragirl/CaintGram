package org.telegram.flux;

import org.telegram.flux.ui.ContainerType;
import org.telegram.flux.ui.FluxContainer;
import org.telegram.flux.ui.containers.BottomBarContainer;
import org.telegram.flux.ui.containers.ChatFooterContainer;
import org.telegram.flux.ui.containers.ChatHeaderContainer;
import org.telegram.flux.ui.containers.ChatMenuContainer;
import org.telegram.flux.ui.containers.MessageMenuContainer;
import org.telegram.flux.ui.containers.SettingsContainer;
import org.telegram.flux.ui.containers.SidebarContainer;

/**
 * Public entry point of the Flux API.
 *
 * Flux.UI exposes one container per native Telegram UI location. Callers never
 * touch a Telegram-internal class directly - they only ever see Flux's own
 * component/container types, which internally translate into native UI calls
 * once a real Telegram screen binds itself as a host (see {@code bindHost}
 * on each container).
 *
 * Usage:
 * <pre>
 *   String id = Flux.UI.ChatHeader.add(new FluxViewComponent(myBannerView));
 *   ...
 *   Flux.UI.ChatHeader.remove(id);
 * </pre>
 */
public final class Flux {

    private Flux() {
    }

    /**
     * Container registry for every native Telegram UI location Flux currently
     * supports. Adding a new location later is just one more field here plus
     * one new AbstractFluxContainer subclass - existing callers are unaffected.
     */
    public static final class UI {

        private UI() {
        }

        public static final ChatHeaderContainer ChatHeader = new ChatHeaderContainer();
        public static final ChatFooterContainer ChatFooter = new ChatFooterContainer();
        public static final ChatMenuContainer ChatMenu = new ChatMenuContainer();
        public static final MessageMenuContainer MessageMenu = new MessageMenuContainer();
        public static final SidebarContainer Sidebar = new SidebarContainer();
        public static final BottomBarContainer BottomBar = new BottomBarContainer();
        public static final SettingsContainer Settings = new SettingsContainer();

        private static final FluxContainer<?>[] ALL = {
                ChatHeader, ChatFooter, ChatMenu, MessageMenu, Sidebar, BottomBar, Settings
        };

        /** Looks up a container by its {@link ContainerType}, for generic/plugin code that doesn't know the concrete field name. */
        public static FluxContainer<?> get(ContainerType type) {
            for (FluxContainer<?> container : ALL) {
                if (container.getType() == type) {
                    return container;
                }
            }
            throw new IllegalArgumentException("Unknown Flux.UI container type: " + type);
        }

        /** Removes every component from every container. Useful when a plugin/mod feature is unloaded or the app resets. */
        public static void clearAll() {
            for (FluxContainer<?> container : ALL) {
                container.clear();
            }
        }
    }
}
