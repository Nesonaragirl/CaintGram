package com.fluxgram.api;

/**
 * Flux.Utils
 *
 * Reserved for shared helper functionality (formatting, logging helpers,
 * etc.) used across the other Flux modules. Currently exposes only the
 * wrapper version so callers have something stable to depend on while the
 * rest of the API is built out.
 */
public final class FluxUtils {

    private static final String VERSION = "0.1.0";

    FluxUtils() {
    }

    /**
     * Returns the current version of the Flux API wrapper.
     *
     * @return the wrapper version string
     */
    public String getVersion() {
        return VERSION;
    }
}
