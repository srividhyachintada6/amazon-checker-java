package com.amazonchecker.utils;

/**
 * Simple data holder representing one row from products.csv
 * (name, url) - equivalent of the dict used in the Python version.
 */
public class Product {
    private final String name;
    private final String url;

    public Product(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean hasUrl() {
        return url != null && !url.isBlank();
    }
}
