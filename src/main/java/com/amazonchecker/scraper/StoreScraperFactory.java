package com.amazonchecker.scraper;

import com.amazonchecker.model.Store;
import com.amazonchecker.web.dto.SearchResultResponse;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * Factory and registry managing ProductStoreScraper instances.
 * Enables extensible multi-store resolution and multi-store searching.
 */
@Component
public class StoreScraperFactory {

    private final Map<Store, ProductStoreScraper> scrapers = new EnumMap<>(Store.class);

    public StoreScraperFactory(List<ProductStoreScraper> scraperList) {
        for (ProductStoreScraper scraper : scraperList) {
            scrapers.put(scraper.getStore(), scraper);
        }
    }

    /**
     * Retrieves the scraper for a specific store.
     */
    public ProductStoreScraper getScraper(Store store) {
        ProductStoreScraper scraper = scrapers.get(store);
        if (scraper == null) {
            throw new IllegalArgumentException("No scraper registered for store: " + store);
        }
        return scraper;
    }

    /**
     * Resolves the appropriate scraper by inspecting the product URL.
     */
    public ProductStoreScraper getScraperForUrl(String url) {
        Store store = detectStore(url);
        return getScraper(store);
    }

    /**
     * Detects store enum from a product URL.
     */
    public Store detectStore(String url) {
        return Store.fromUrl(url);
    }

    /**
     * Searches products across one or all supported stores.
     *
     * @param query       the search term
     * @param storeFilter "amazon", "flipkart", "both", or "all"
     * @return combined list of search result items
     */
    public List<SearchResultResponse> search(String query, String storeFilter) {
        if (query == null || query.trim().isBlank()) {
            return Collections.emptyList();
        }

        String filter = (storeFilter != null ? storeFilter.trim().toLowerCase() : "all");

        if ("amazon".equals(filter)) {
            ProductStoreScraper amazon = scrapers.get(Store.AMAZON);
            return amazon != null ? amazon.searchProducts(query) : Collections.emptyList();
        }

        if ("flipkart".equals(filter)) {
            ProductStoreScraper flipkart = scrapers.get(Store.FLIPKART);
            return flipkart != null ? flipkart.searchProducts(query) : Collections.emptyList();
        }

        // Search both stores concurrently
        List<SearchResultResponse> combined = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<List<SearchResultResponse>> amazonFuture = executor.submit(() -> {
                ProductStoreScraper s = scrapers.get(Store.AMAZON);
                return s != null ? s.searchProducts(query) : Collections.emptyList();
            });

            Future<List<SearchResultResponse>> flipkartFuture = executor.submit(() -> {
                ProductStoreScraper s = scrapers.get(Store.FLIPKART);
                return s != null ? s.searchProducts(query) : Collections.emptyList();
            });

            try {
                combined.addAll(amazonFuture.get(25, TimeUnit.SECONDS));
            } catch (Exception e) {
                System.err.println("⚠ Amazon search task failed or timed out: " + e.getMessage());
            }

            try {
                combined.addAll(flipkartFuture.get(25, TimeUnit.SECONDS));
            } catch (Exception e) {
                System.err.println("⚠ Flipkart search task failed or timed out: " + e.getMessage());
            }

        } finally {
            executor.shutdownNow();
        }

        return combined;
    }
}
