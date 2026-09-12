package com.amazonchecker.tracker;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Equivalent of tracker/availability_tracker.py
 */
public class AvailabilityTracker {

    private static final String[] SELECTOR_IDS = {
            "availability",
            "availabilityInsideBuyBox",
            "outOfStock"
    };

    public static String getAvailability(Document doc) {
        if (doc == null) return "NOT FOUND";

        for (String id : SELECTOR_IDS) {
            Element div = doc.selectFirst("div#" + id);
            if (div != null) {
                String text = div.text().trim();
                if (!text.isEmpty()) {
                    if (text.toLowerCase().contains("in stock")) return "IN STOCK";
                    if (text.contains("Currently unavailable")) return "UNAVAILABLE";
                    if (text.toLowerCase().contains("out of stock")) return "OUT OF STOCK";
                    return text;
                }
            }
        }

        // Fallback: search globally across the page text
        String pageText = doc.text().toLowerCase();
        if (pageText.contains("in stock")) return "IN STOCK";
        if (pageText.contains("currently unavailable")) return "UNAVAILABLE";

        return "NOT FOUND";
    }
}
