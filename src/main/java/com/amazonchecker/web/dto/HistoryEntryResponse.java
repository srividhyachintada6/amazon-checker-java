package com.amazonchecker.web.dto;

/**
 * Global audit history log entry DTO.
 */
public class HistoryEntryResponse {

    private String timestamp;
    private String productName;
    private String status;
    private Double price;
    private String formattedPrice;

    public HistoryEntryResponse() {
    }

    public HistoryEntryResponse(String timestamp, String productName, String status, Double price, String formattedPrice) {
        this.timestamp = timestamp;
        this.productName = productName;
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
