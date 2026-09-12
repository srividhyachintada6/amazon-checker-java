package com.amazonchecker.scraper;

import com.amazonchecker.model.Store;
import com.amazonchecker.tracker.AvailabilityTracker;
import com.amazonchecker.tracker.PriceTracker;
import com.amazonchecker.utils.Screenshot;
import com.amazonchecker.web.dto.ScrapeResult;
import com.amazonchecker.web.dto.SearchResultResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Concrete ProductStoreScraper implementation for Amazon.
 * Reuses the existing, tested Amazon scraping logic and screenshot capture.
 */
@Component
public class AmazonScraper implements ProductStoreScraper {

    @Override
    public Store getStore() {
        return Store.AMAZON;
    }

    @Override
    public boolean supportsUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.contains("amazon.") || lower.contains("amzn.in") || lower.contains("amzn.to");
    }

    @Override
    public ScrapeResult checkProduct(String url, String productName) {
        ScrapeResult result = new ScrapeResult();
        result.setStore(Store.AMAZON);
        result.setProductUrl(url);
        result.setProductName(productName);

        Document doc = ProductScraper.fetchProductPage(url);
        if (doc == null) {
            result.setAvailability("ERROR");
            result.setStatusMessage("Failed to connect to Amazon product page");
            return result;
        }

        // Availability
        String availability = AvailabilityTracker.getAvailability(doc);
        result.setAvailability(availability != null ? availability : "NOT FOUND");

        // Price
        String priceStr = PriceTracker.getPrice(doc);
        if (priceStr != null && !priceStr.equals("PRICE NOT FOUND")) {
            try {
                String clean = priceStr.replaceAll("[^0-9.]", "");
                if (!clean.isEmpty()) {
                    result.setPrice(Double.parseDouble(clean));
                }
            } catch (NumberFormatException ignored) {}
        }

        // Image URL
        Element imgEl = doc.selectFirst("#landingImage, #imgBlkFront, #main-image, img[data-a-dynamic-image]");
        if (imgEl != null) {
            String src = imgEl.attr("src");
            if (src == null || src.isBlank()) {
                src = imgEl.attr("data-old-hires");
            }
            if (src != null && !src.isBlank()) {
                result.setImageUrl(src);
            }
        }

        // Screenshot
        try {
            String screenshotPath = Screenshot.takeScreenshot(url, productName);
            result.setScreenshotUrl(screenshotPath);
            result.setStatusMessage("Checked successfully with screenshot");
        } catch (Exception e) {
            result.setStatusMessage("Checked without screenshot: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<SearchResultResponse> searchProducts(String query) {
        List<SearchResultResponse> results = SearchScraper.searchProducts(query);
        for (SearchResultResponse item : results) {
            item.setStore("AMAZON");
        }
        return results;
    }
}
