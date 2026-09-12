package com.amazonchecker.web.dto;

/**
 * Historical price and availability data point.
 */
public class PriceHistoryPoint {

    private String timestamp;
    private Double price;
    private String status;

    public PriceHistoryPoint() {
    }

    public PriceHistoryPoint(String timestamp, Double price, String status) {
        this.timestamp = timestamp;
        this.price = price;
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
