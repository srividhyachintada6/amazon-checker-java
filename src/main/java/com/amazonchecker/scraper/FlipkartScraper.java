package com.amazonchecker.scraper;

import com.amazonchecker.model.Store;
import com.amazonchecker.web.dto.ScrapeResult;
import com.amazonchecker.web.dto.SearchResultResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Concrete ProductStoreScraper implementation for Flipkart.
 * Uses Headless Chrome via Selenium to reliably bypass Akamai bot-detection (which blocks Jsoup with HTTP 403),
 * extract real prices and availability, and capture proof screenshots.
 */
@Component
public class FlipkartScraper implements ProductStoreScraper {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";

    @Override
    public Store getStore() {
        return Store.FLIPKART;
    }

    @Override
    public boolean supportsUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.contains("flipkart.com") || lower.contains("dl.flipkart.com");
    }

    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("user-agent=" + USER_AGENT);
        return options;
    }

    @Override
    public ScrapeResult checkProduct(String url, String productName) {
        ScrapeResult result = new ScrapeResult();
        result.setStore(Store.FLIPKART);
        result.setProductUrl(url);
        result.setProductName(productName);

        WebDriver driver = null;
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver(createChromeOptions());
            driver.get(url);
            Thread.sleep(3500);

            // 1. Determine Title if not already provided
            String pageTitle = driver.getTitle();
            if (productName == null || productName.isBlank()) {
                result.setProductName(cleanFlipkartTitle(pageTitle));
            }

            // 2. Check Availability
            String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();
            if (bodyText.contains("currently unavailable") || bodyText.contains("sold out") || bodyText.contains("out of stock")) {
                result.setAvailability("OUT OF STOCK");
            } else if (bodyText.contains("buy now") || bodyText.contains("add to cart") || bodyText.contains("in stock")) {
                result.setAvailability("IN STOCK");
            } else {
                result.setAvailability("IN STOCK");
            }

            // 3. Extract Price
            Double extractedPrice = null;
            // Common Flipkart price classes: Nx9bqj, _30jeq3, _1vC4e8
            List<WebElement> priceEls = driver.findElements(By.cssSelector("div[class*='Nx9bqj'], div._30jeq3, div._1vC4e8"));
            for (WebElement el : priceEls) {
                String text = el.getText();
                if (text != null && text.contains("₹")) {
                    extractedPrice = parsePrice(text);
                    if (extractedPrice != null) break;
                }
            }

            // Fallback: search for any element containing ₹
            if (extractedPrice == null) {
                List<WebElement> rupeeEls = driver.findElements(By.xpath("//*[contains(text(), '₹')]"));
                for (WebElement el : rupeeEls) {
                    String text = el.getText();
                    if (text != null && text.contains("₹")) {
                        Double p = parsePrice(text);
                        if (p != null && p > 50) { // filter out minor coupon texts like ₹50 off
                            extractedPrice = p;
                            break;
                        }
                    }
                }
            }
            result.setPrice(extractedPrice);

            // 4. Extract Product Image
            try {
                List<WebElement> imgs = driver.findElements(By.cssSelector("img[class*='DByuf4'], img._396cs4, img[src*='rukminim']"));
                if (!imgs.isEmpty()) {
                    String src = imgs.get(0).getAttribute("src");
                    if (src != null && !src.isBlank()) {
                        result.setImageUrl(src);
                    }
                }
            } catch (Exception ignored) {}

            // 5. Capture Proof Screenshot directly from active session
            try {
                File screenshotDir = new File("screenshots");
                if (!screenshotDir.exists()) {
                    screenshotDir.mkdirs();
                }
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String safeName = (productName != null ? productName : "flipkart_item").replaceAll("[^a-zA-Z0-9_-]", "_");
                String filename = "screenshots/flipkart_" + safeName + "_" + timestamp + ".png";

                File srcFile = ((ChromeDriver) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(srcFile.toPath(), Path.of(filename));
                result.setScreenshotUrl(filename);
                result.setStatusMessage("Checked successfully with screenshot");
            } catch (Exception e) {
                result.setStatusMessage("Checked without screenshot: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Flipkart scraping failed for " + url + ": " + e.getMessage());
            result.setAvailability("ERROR");
            result.setStatusMessage("Flipkart check error: " + e.getMessage());
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ignored) {}
            }
        }

        return result;
    }

    @Override
    public List<SearchResultResponse> searchProducts(String query) {
        List<SearchResultResponse> results = new ArrayList<>();
        if (query == null || query.trim().isBlank()) {
            return results;
        }

        WebDriver driver = null;
        try {
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver(createChromeOptions());

            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String searchUrl = "https://www.flipkart.com/search?q=" + encodedQuery;
            driver.get(searchUrl);
            Thread.sleep(3500);

            List<WebElement> links = driver.findElements(By.cssSelector("a[href*='/p/']"));
            Set<String> seenUrls = new HashSet<>();

            for (WebElement link : links) {
                if (results.size() >= 10) break;
                try {
                    String href = link.getAttribute("href");
                    if (href == null || href.isBlank()) continue;

                    // Clean URL
                    String cleanUrl = href.split("\\?")[0];
                    if (seenUrls.contains(cleanUrl)) continue;
                    seenUrls.add(cleanUrl);

                    String text = link.getText().trim();
                    if (text.isBlank() || text.length() < 5) continue;

                    // Split text lines if text contains multiple details
                    String title = text.split("\n")[0].trim();
                    if (title.equalsIgnoreCase("Currently unavailable") || title.equalsIgnoreCase("Price: Not Available")) {
                        // Look inside for title element
                        try {
                            WebElement titleEl = link.findElement(By.cssSelector("div[class*='WKTcLC'], div._4rR01T, div.s1Q9rs"));
                            if (titleEl != null && !titleEl.getText().isBlank()) {
                                title = titleEl.getText().trim();
                            }
                        } catch (Exception ignored) {}
                    }
                    if (title.isBlank() || title.length() < 3) continue;

                    // Extract price inside card
                    Double price = null;
                    String formattedPrice = "Price unavailable";
                    try {
                        WebElement priceEl = link.findElement(By.xpath(".//div[contains(text(), '₹')]"));
                        if (priceEl != null) {
                            String pText = priceEl.getText().trim();
                            price = parsePrice(pText);
                            if (price != null) {
                                formattedPrice = "₹" + String.format(Locale.US, "%,.2f", price);
                            }
                        }
                    } catch (Exception ignored) {}

                    // Extract image
                    String imgUrl = null;
                    try {
                        WebElement img = link.findElement(By.cssSelector("img[src*='rukminim']"));
                        if (img != null) {
                            imgUrl = img.getAttribute("src");
                        }
                    } catch (Exception ignored) {}

                    String availability = text.toLowerCase().contains("currently unavailable") ? "OUT OF STOCK" : "IN STOCK";

                    SearchResultResponse item = new SearchResultResponse(
                            title,
                            price,
                            formattedPrice,
                            availability,
                            cleanUrl,
                            imgUrl,
                            "FLIPKART"
                    );
                    results.add(item);
                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.err.println("❌ Flipkart search failed for query [" + query + "]: " + e.getMessage());
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ignored) {}
            }
        }

        return results;
    }

    private String cleanFlipkartTitle(String pageTitle) {
        if (pageTitle == null) return "Flipkart Product";
        String cleaned = pageTitle.replace(": Flipkart.com", "").replace("- Flipkart.com", "").trim();
        int atIdx = cleaned.indexOf(" at Best Price in India");
        if (atIdx != -1) {
            cleaned = cleaned.substring(0, atIdx).trim();
        }
        return cleaned.isBlank() ? "Flipkart Product" : cleaned;
    }

    private Double parsePrice(String text) {
        if (text == null) return null;
        try {
            // Extracts first sequence of digits possibly with commas/dots
            String digits = text.replaceAll("[^0-9.]", "").trim();
            if (!digits.isEmpty()) {
                return Double.parseDouble(digits);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
