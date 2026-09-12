package com.amazonchecker.scraper;

import com.amazonchecker.config.Headers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Equivalent of scraper/search_scraper.py
 */
public class SearchScraper {

    public static String searchProduct(String productName) {
        String query = URLEncoder.encode(productName, StandardCharsets.UTF_8).replace("+", "%20");
        // Python version used a plain "+" join; Amazon accepts either, we keep it simple:
        query = productName.trim().replace(" ", "+");
        String url = "https://www.amazon.in/s?k=" + query;

        try {
            Document doc = Jsoup.connect(url)
                    .headers(Headers.get())
                    .timeout(10_000)
                    .get();

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("href");
                // Amazon product URLs always contain '/dp/'
                if (href.contains("/dp/")) {
                    String cleanHref = href.split("\\?")[0];
                    return "https://www.amazon.in" + cleanHref;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Search request failed: " + e.getMessage());
        }
        return null;
    }

    public static Document fetchProductPage(String url) {
        try {
            return Jsoup.connect(url)
                    .headers(Headers.get())
                    .timeout(10_000)
                    .get();
        } catch (IOException e) {
            return null;
        }
    }
}
