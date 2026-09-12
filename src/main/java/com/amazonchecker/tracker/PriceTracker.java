package com.amazonchecker.tracker;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Equivalent of tracker/price_tracker.py
 * Extracts a product's price from an Amazon product page (works for online or offline HTML).
 */
public class PriceTracker {

    private static final String[] PRICE_IDS = {
            "priceblock_ourprice",
            "priceblock_dealprice",
            "priceblock_saleprice"
    };

    public static String getPrice(Document doc) {
        if (doc == null) return "PRICE NOT FOUND";

        Elements priceSpans = doc.select("span.a-offscreen");
        for (Element span : priceSpans) {
            String priceText = span.text().trim();
            if (priceText.contains("₹")) {
                return clean(priceText);
            }
        }

        for (String id : PRICE_IDS) {
            Element price = doc.selectFirst("span#" + id);
            if (price != null) {
                return clean(price.text());
            }
        }

        Element dealPrice = doc.selectFirst("span.a-price span.a-offscreen");
        if (dealPrice != null) {
            return clean(dealPrice.text());
        }

        return "PRICE NOT FOUND";
    }

    private static String clean(String text) {
        return text.replace("₹", "").replace(",", "").trim();
    }
}
