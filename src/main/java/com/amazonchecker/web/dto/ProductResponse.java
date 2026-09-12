package com.amazonchecker.web.dto;

/**
 * Structured product result matching REST API requirements.
 */
public class ProductResponse {

    private String id;
    private String name;
    private String store;
    private String status;
    private Double price;
    private Double previousPrice;
    private Double priceChange;
    private String lastChecked;
    private String productUrl;
    private String screenshot;

    public ProductResponse() {
    }

    public ProductResponse(String id, String name, String status, Double price, Double previousPrice,
                           Double priceChange, String lastChecked, String productUrl, String screenshot) {
        this(id, name, "AMAZON", status, price, previousPrice, priceChange, lastChecked, productUrl, screenshot);
    }

    public ProductResponse(String id, String name, String store, String status, Double price, Double previousPrice,
                           Double priceChange, String lastChecked, String productUrl, String screenshot) {
        this.id = id;
        this.name = name;
        this.store = store;
        this.status = status;
        this.price = price;
        this.previousPrice = previousPrice;
        this.priceChange = priceChange;
        this.lastChecked = lastChecked;
        this.productUrl = productUrl;
        this.screenshot = screenshot;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(Double previousPrice) {
        this.previousPrice = previousPrice;
    }

    public Double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(Double priceChange) {
        this.priceChange = priceChange;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getUrl() {
        return productUrl;
    }

    public void setUrl(String url) {
        this.productUrl = url;
    }

    public String getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }
}
