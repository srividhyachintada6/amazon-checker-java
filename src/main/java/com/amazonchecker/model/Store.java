package com.amazonchecker.model;

import java.net.URI;

/**
 * Enumeration representing supported e-commerce stores.
 */
public enum Store {
    AMAZON("Amazon", "https://www.amazon.in"),
    FLIPKART("Flipkart", "https://www.flipkart.com");

    private final String displayName;
    private final String baseUrl;

    Store(String displayName, String baseUrl) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Automatically detects the Store from a given product URL.
     *
     * @param url the product URL
     * @return the detected Store
     * @throws IllegalArgumentException if the URL does not belong to any supported store
     */
    public static Store fromUrl(String url) {
        if (url == null || url.trim().isBlank()) {
            throw new IllegalArgumentException("Product URL cannot be empty");
        }
        String cleanUrl = url.trim().toLowerCase();

        if (cleanUrl.contains("amazon.") || cleanUrl.contains("amzn.in") || cleanUrl.contains("amzn.to")) {
            return AMAZON;
        }
        if (cleanUrl.contains("flipkart.com") || cleanUrl.contains("dl.flipkart.com")) {
            return FLIPKART;
        }

        throw new IllegalArgumentException("Unsupported store URL: " + url + ". Supported stores are Amazon and Flipkart.");
    }

    /**
     * Parses a store string (case-insensitive) with fallback to AMAZON.
     */
    public static Store fromString(String name) {
        if (name == null || name.trim().isBlank()) {
            return AMAZON;
        }
        String clean = name.trim().toUpperCase();
        for (Store store : values()) {
            if (store.name().equalsIgnoreCase(clean) || store.displayName.equalsIgnoreCase(clean)) {
                return store;
            }
        }
        return AMAZON;
    }
}
