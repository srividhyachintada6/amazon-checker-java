package com.amazonchecker.scraper;

import com.amazonchecker.config.Headers;
import com.amazonchecker.web.dto.SearchResultResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public static List<SearchResultResponse> searchProducts(String productName) {
        List<SearchResultResponse> results = new ArrayList<>();
        if (productName == null || productName.trim().isBlank()) {
            return results;
        }

        String query = productName.trim().replace(" ", "+");
        String url = "https://www.amazon.in/s?k=" + query;

        try {
            Document doc = Jsoup.connect(url)
                    .headers(Headers.get())
                    .timeout(15_000)
                    .get();

            Elements items = doc.select("div[data-component-type='s-search-result'], div.s-result-item[data-asin]");
            Set<String> seenAsins = new HashSet<>();

            for (Element item : items) {
                String asin = item.attr("data-asin");
                if (asin == null || asin.isBlank() || seenAsins.contains(asin)) continue;

                // Title
                String title = "";
                Element h2 = item.selectFirst("h2");
                if (h2 != null && !h2.text().isBlank()) {
                    title = h2.text().trim();
                } else {
                    Element titleEl = item.selectFirst("span.a-text-normal, a.a-link-normal span");
                    if (titleEl != null && !titleEl.text().isBlank()) {
                        title = titleEl.text().trim();
                    } else {
                        Element img = item.selectFirst("img.s-image");
                        if (img != null && !img.attr("alt").isBlank()) {
                            title = img.attr("alt").trim();
                        }
                    }
                }
                if (title.isBlank()) continue;

                // URL
                String productUrl = "https://www.amazon.in/dp/" + asin;
                Element linkEl = item.selectFirst("h2 a[href], a.a-link-normal[href*='/dp/'], a[href*='/dp/']");
                if (linkEl != null) {
                    String href = linkEl.attr("href");
                    if (!href.isBlank()) {
                        if (href.startsWith("/")) {
                            href = "https://www.amazon.in" + href;
                        }
                        String clean = href.split("\\?")[0];
                        if (clean.contains("/dp/")) {
                            productUrl = clean;
                        }
                    }
                }

                // Image URL
                String imageUrl = null;
                Element imgEl = item.selectFirst("img.s-image");
                if (imgEl != null) {
                    String src = imgEl.attr("src");
                    if (!src.isBlank()) {
                        imageUrl = src;
                    }
                }

                // Price
                Double price = null;
                String formattedPrice = null;
                Element offscreenPrice = item.selectFirst("span.a-price span.a-offscreen");
                String rawPrice = null;
                if (offscreenPrice != null && !offscreenPrice.text().isBlank()) {
                    rawPrice = offscreenPrice.text().trim();
                } else {
                    Element wholePrice = item.selectFirst("span.a-price-whole");
                    if (wholePrice != null && !wholePrice.text().isBlank()) {
                        rawPrice = "₹" + wholePrice.text().trim();
                    }
                }

                if (rawPrice != null) {
                    String clean = rawPrice.replace("₹", "").replace(",", "").trim();
                    try {
                        price = Double.parseDouble(clean);
                        formattedPrice = "₹" + String.format(Locale.ENGLISH, "%,.2f", price);
                    } catch (Exception ignored) {
                        formattedPrice = rawPrice;
                    }
                }

                // Availability
                String availability = "IN STOCK";
                String itemText = item.text().toLowerCase();
                if (itemText.contains("currently unavailable") || itemText.contains("out of stock")) {
                    availability = "UNAVAILABLE";
                } else if (price == null) {
                    availability = "CHECK ON AMAZON";
                }

                seenAsins.add(asin);
                results.add(new SearchResultResponse(title, price, formattedPrice, availability, productUrl, imageUrl));

                if (results.size() >= 10) {
                    break;
                }
            }

            // Fallback: If 0 items matched via data-component-type, parse generic /dp/ links
            if (results.isEmpty()) {
                Elements links = doc.select("a[href*='/dp/']");
                for (Element link : links) {
                    String href = link.attr("href");
                    String cleanHref = href.split("\\?")[0];
                    if (cleanHref.startsWith("/")) cleanHref = "https://www.amazon.in" + cleanHref;

                    String linkText = link.text().trim();
                    if (!linkText.isBlank() && linkText.length() > 5) {
                        results.add(new SearchResultResponse(linkText, null, null, "IN STOCK", cleanHref, null));
                        if (results.size() >= 5) break;
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Amazon search scraping failed: " + e.getMessage());
        }

        return results;
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
