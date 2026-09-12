package com.amazonchecker.web.dto;

/**
 * Global audit history log entry DTO with store identification.
 */
public class HistoryEntryResponse {

    private String timestamp;
    private String productName;
    private String store;
    private String status;
    private Double price;
    private String formattedPrice;

    public HistoryEntryResponse() {
    }

    public HistoryEntryResponse(String timestamp, String productName, String status, Double price, String formattedPrice) {
        this(timestamp, productName, "AMAZON", status, price, formattedPrice);
    }

    public HistoryEntryResponse(String timestamp, String productName, String store, String status, Double price, String formattedPrice) {
        this.timestamp = timestamp;
        this.productName = productName;
        this.store = store != null ? store : "AMAZON";
        this.status = status;
        this.price = price;
        this.formattedPrice = formattedPrice;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public void setFormattedPrice(String formattedPrice) {
        this.formattedPrice = formattedPrice;
    }
}
