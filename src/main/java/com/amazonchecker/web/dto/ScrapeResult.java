package com.amazonchecker.web.dto;

import com.amazonchecker.model.Store;

import java.time.LocalDateTime;

/**
 * Universal product scrape and check result across all supported stores.
 */
public class ScrapeResult {

    private String productName;
    private Store store;
    private String productUrl;
    private String imageUrl;
    private Double price;
    private String availability;
    private LocalDateTime checkedAt;
    private String statusMessage;
    private String screenshotUrl;

    public ScrapeResult() {
        this.checkedAt = LocalDateTime.now();
    }

    public ScrapeResult(String productName, Store store, String productUrl, String imageUrl,
                        Double price, String availability, String statusMessage, String screenshotUrl) {
        this.productName = productName;
        this.store = store;
        this.productUrl = productUrl;
        this.imageUrl = imageUrl;
        this.price = price;
        this.availability = availability;
        this.checkedAt = LocalDateTime.now();
        this.statusMessage = statusMessage;
        this.screenshotUrl = screenshotUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }
}
