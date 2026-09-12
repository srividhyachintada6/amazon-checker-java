package com.amazonchecker.web.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing price comparison across stores for matching products.
 */
public class PriceComparisonResponse {

    private String title;
    private Double lowestPrice;
    private String lowestStore;
    private String lowestUrl;
    private Double highestPrice;
    private String highestStore;
    private String highestUrl;
    private Double priceDifference;
    private List<ComparedProductItem> items = new ArrayList<>();

    public PriceComparisonResponse() {
    }

    public PriceComparisonResponse(String title) {
        this.title = title;
    }

    public static class ComparedProductItem {
        private String id;
        private String name;
        private String store;
        private Double price;
        private String formattedPrice;
        private String availability;
        private String url;
        private String imageUrl;

        public ComparedProductItem() {
        }

        public ComparedProductItem(String id, String name, String store, Double price,
                                   String formattedPrice, String availability, String url, String imageUrl) {
            this.id = id;
            this.name = name;
            this.store = store;
            this.price = price;
            this.formattedPrice = formattedPrice;
            this.availability = availability;
            this.url = url;
            this.imageUrl = imageUrl;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStore() { return store; }
        public void setStore(String store) { this.store = store; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getFormattedPrice() { return formattedPrice; }
        public void setFormattedPrice(String formattedPrice) { this.formattedPrice = formattedPrice; }
        public String getAvailability() { return availability; }
        public void setAvailability(String availability) { this.availability = availability; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public void addItem(ComparedProductItem item) {
        items.add(item);
        recalculate();
    }

    public void recalculate() {
        if (items.isEmpty()) return;

        Double min = null;
        String minStore = null;
        String minUrl = null;

        Double max = null;
        String maxStore = null;
        String maxUrl = null;

        for (ComparedProductItem item : items) {
            if (item.getPrice() != null && item.getPrice() > 0) {
                if (min == null || item.getPrice() < min) {
                    min = item.getPrice();
                    minStore = item.getStore();
                    minUrl = item.getUrl();
                }
                if (max == null || item.getPrice() > max) {
                    max = item.getPrice();
                    maxStore = item.getStore();
                    maxUrl = item.getUrl();
                }
            }
        }

        this.lowestPrice = min;
        this.lowestStore = minStore;
        this.lowestUrl = minUrl;
        this.highestPrice = max;
        this.highestStore = maxStore;
        this.highestUrl = maxUrl;

        if (min != null && max != null) {
            this.priceDifference = Math.round((max - min) * 100.0) / 100.0;
        } else {
            this.priceDifference = 0.0;
        }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Double getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(Double lowestPrice) { this.lowestPrice = lowestPrice; }
    public String getLowestStore() { return lowestStore; }
    public void setLowestStore(String lowestStore) { this.lowestStore = lowestStore; }
    public String getLowestUrl() { return lowestUrl; }
    public void setLowestUrl(String lowestUrl) { this.lowestUrl = lowestUrl; }
    public Double getHighestPrice() { return highestPrice; }
    public void setHighestPrice(Double highestPrice) { this.highestPrice = highestPrice; }
    public String getHighestStore() { return highestStore; }
    public void setHighestStore(String highestStore) { this.highestStore = highestStore; }
    public String getHighestUrl() { return highestUrl; }
    public void setHighestUrl(String highestUrl) { this.highestUrl = highestUrl; }
    public Double getPriceDifference() { return priceDifference; }
    public void setPriceDifference(Double priceDifference) { this.priceDifference = priceDifference; }
    public List<ComparedProductItem> getItems() { return items; }
    public void setItems(List<ComparedProductItem> items) { this.items = items; recalculate(); }
}
