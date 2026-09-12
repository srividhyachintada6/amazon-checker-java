package com.amazonchecker.config;

/**
 * Equivalent of config/settings.py
 */
public class Settings {
    public static final int CHECK_INTERVAL_HOURS = 6;
    public static final int DEFAULT_SCHEDULE_MINUTES = 30;

    /** Set true to parse the local offline sample page instead of hitting the network. */
    public static final boolean USE_OFFLINE_DEMO = false;
}
