package com.caint.api;

/**
 * Caint.Utils
 *
 * Reserved for shared helper functionality (formatting, logging helpers,
 * etc.) used across the other Caint modules. Currently exposes only the
 * wrapper version so callers have something stable to depend on while the
 * rest of the API is built out.
 */
public final class CaintUtils {

    private static final String VERSION = "0.1.0";

    CaintUtils() {
    }

    /**
     * Returns the current version of the Caint API wrapper.
     *
     * @return the wrapper version string
     */
    public String getVersion() {
        return VERSION;
    }
}
