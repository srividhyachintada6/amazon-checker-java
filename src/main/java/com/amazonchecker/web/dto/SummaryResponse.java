package com.amazonchecker.web.dto;

/**
 * Summary statistics response DTO including price drops.
 */
public class SummaryResponse {

    private int totalProducts;
    private int inStock;
    private int outOfStock;
    private int errors;
    private int priceDrops;
    private String lastChecked;

    public SummaryResponse() {
    }

    public SummaryResponse(int totalProducts, int inStock, int outOfStock, int errors, int priceDrops, String lastChecked) {
        this.totalProducts = totalProducts;
        this.inStock = inStock;
        this.outOfStock = outOfStock;
        this.errors = errors;
        this.priceDrops = priceDrops;
        this.lastChecked = lastChecked;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getInStock() {
        return inStock;
    }

    public void setInStock(int inStock) {
        this.inStock = inStock;
    }

    public int getOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(int outOfStock) {
        this.outOfStock = outOfStock;
    }

    public int getPriceDrops() {
        return priceDrops;
    }

    public void setPriceDrops(int priceDrops) {
        this.priceDrops = priceDrops;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
    }
}
