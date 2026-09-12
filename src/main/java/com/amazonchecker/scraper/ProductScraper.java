package com.amazonchecker.scraper;

import com.amazonchecker.config.Headers;
import com.amazonchecker.config.Settings;
import com.amazonchecker.utils.MockParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

/**
 * Equivalent of scraper/product_scraper.py
 */
public class ProductScraper {

    private static final String OFFLINE_DIR = "offline_pages";

    public static String normalizeUrl(String url) {
        url = url.trim();
        if (url.startsWith("amazon.")) {
            return "https://" + url;
        } else if (!url.startsWith("http")) {
            return "https://" + url;
        }
        return url;
    }

    /** Simple version: always fetches live, matching main.py's default call. */
    public static Document fetchProductPage(String url) {
        try {
            String normalized = normalizeUrl(url);
            return Jsoup.connect(normalized)
                    .headers(Headers.get())
                    .timeout(10_000)
                    .get();
        } catch (IOException e) {
            System.err.println("❌ Failed to fetch product page: " + e.getMessage());
            return null;
        }
    }

    /** Extended version supporting the offline demo file, mirroring the Python function signature. */
    public static Document fetchProductPage(String url, String offlineFile) throws IOException {
        if (Settings.USE_OFFLINE_DEMO && offlineFile != null) {
            String path = OFFLINE_DIR + File.separator + offlineFile;
            return MockParser.parseMockHtml(path);
        }
        return fetchProductPage(url);
    }
}
