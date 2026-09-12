package com.amazonchecker.utils;

/**
 * Data holder representing one row from products.csv
 * (name, url, optional store).
 */
public class Product {
    private final String name;
    private final String url;
    private final String store;

    public Product(String name, String url) {
        this(name, url, "AMAZON");
    }

    public Product(String name, String url, String store) {
        this.name = name;
        this.url = url;
        this.store = store != null ? store : "AMAZON";
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getStore() {
        return store;
    }

    public boolean hasUrl() {
        return url != null && !url.isBlank();
    }
}
