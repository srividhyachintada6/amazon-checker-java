package com.amazonchecker.web.dto;

/**
 * Request DTO for creating and editing monitored products across stores.
 */
public class ProductRequest {

    private String name;
    private String url;
    private String store;

    public ProductRequest() {
    }

    public ProductRequest(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public ProductRequest(String name, String url, String store) {
        this.name = name;
        this.url = url;
        this.store = store;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }
}
