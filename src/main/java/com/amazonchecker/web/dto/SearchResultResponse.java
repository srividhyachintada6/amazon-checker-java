package com.amazonchecker.web.dto;

/**
 * Data Transfer Object for real Amazon search results.
 */
public class SearchResultResponse {

    private String name;
    private Double price;
    private String formattedPrice;
    private String availability;
    private String url;
    private String imageUrl;

    public SearchResultResponse() {
    }

    public SearchResultResponse(String name, Double price, String formattedPrice, String availability, String url, String imageUrl) {
        this.name = name;
        this.price = price;
        this.formattedPrice = formattedPrice;
        this.availability = availability;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
