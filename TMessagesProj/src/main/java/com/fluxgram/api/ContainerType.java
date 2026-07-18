package com.fluxgram.api;

/**
 * Every native Telegram UI location a Flux.UI container can represent.
 * Adding a new location later is just one more enum value plus one more
 * field in Flux.UI -- existing containers/callers are unaffected.
 */
public enum ContainerType {
    CHAT_HEADER,
    CHAT_FOOTER,
    CHAT_MENU,
    MESSAGE_MENU,
    SIDEBAR,
    BOTTOM_BAR,
    SETTINGS
}
