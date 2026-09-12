package com.amazonchecker.scraper;

import com.amazonchecker.model.Store;
import com.amazonchecker.web.dto.ScrapeResult;
import com.amazonchecker.web.dto.SearchResultResponse;

import java.util.List;

/**
 * Common abstraction for store scrapers (Amazon, Flipkart, etc.)
 */
public interface ProductStoreScraper {

    /**
     * Returns the store handled by this scraper.
     */
    Store getStore();

    /**
     * Returns true if the URL belongs to this store.
     */
    boolean supportsUrl(String url);

    /**
     * Checks product availability, price, and captures proof.
     */
    ScrapeResult checkProduct(String url, String productName);

    /**
     * Searches the store for products matching the query.
     */
    List<SearchResultResponse> searchProducts(String query);
}
