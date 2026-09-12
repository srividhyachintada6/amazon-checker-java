package com.amazonchecker.web.dto;

/**
 * Request DTO for creating and editing monitored products.
 */
public class ProductRequest {

    private String name;
    private String url;

    public ProductRequest() {
    }

    public ProductRequest(String name, String url) {
        this.name = name;
        this.url = url;
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
}
