package com.amazonchecker;

import com.amazonchecker.scraper.ProductScraper;
import com.amazonchecker.scraper.SearchScraper;
import com.amazonchecker.tracker.AvailabilityTracker;
import com.amazonchecker.tracker.PriceTracker;
import com.amazonchecker.utils.CsvHandler;
import com.amazonchecker.utils.LogWriter;
import com.amazonchecker.utils.Product;
import com.amazonchecker.utils.Screenshot;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Equivalent of main.py
 * Reads products from data/products.csv, checks availability + price for each,
 * logs the result, and saves a screenshot.
 */
public class Main {

    public static void run() {
        List<Product> products;
        try {
            products = CsvHandler.readProducts("data/products.csv");
        } catch (Exception e) {
            System.err.println("❌ Could not read data/products.csv: " + e.getMessage());
            return;
        }

        for (Product product : products) {
            String name = product.getName();
            String url = product.getUrl();

            System.out.println("\n🔍 Checking: " + name);

            if (!product.hasUrl()) {
                url = SearchScraper.searchProduct(name);
                if (url == null) {
                    System.out.println("❌ Product not found via search");
                    continue;
                }
            }

            Document doc = ProductScraper.fetchProductPage(url);
            if (doc == null) {
                System.out.println("❌ Unable to fetch product page");
                continue;
            }

            String availability = AvailabilityTracker.getAvailability(doc);
            String price = PriceTracker.getPrice(doc);

            System.out.println("📦 Availability: " + availability);
            System.out.println("💰 Price: ₹" + price);

            LogWriter.logResult(name, availability, price);

            try {
                String screenshot = Screenshot.takeScreenshot(url, name);
                System.out.println("📸 Screenshot saved: " + screenshot);
            } catch (Exception e) {
                System.out.println("⚠️  Screenshot skipped: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        run();
    }
}
