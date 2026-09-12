package com.amazonchecker.web;

import com.amazonchecker.utils.CsvHandler;
import com.amazonchecker.utils.Product;
import com.amazonchecker.web.dto.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class ProductService {

    private static final String CSV_FILE = "data/products.csv";
    private static final String LOG_FILE = "data/availability_log.txt";
    private static final String SCREENSHOTS_DIR = "screenshots";

    public static class LogEntry {
        final String timestamp;
        final String status;
        final String priceStr;

        LogEntry(String timestamp, String status, String priceStr) {
            this.timestamp = timestamp;
            this.status = status;
            this.priceStr = priceStr;
        }
    }

    public List<ProductResponse> getProducts() {
        // 1. Read configured products from CSV
        List<Product> configuredProducts = new ArrayList<>();
        try {
            Path csvPath = Path.of(CSV_FILE);
            if (Files.exists(csvPath)) {
                configuredProducts = CsvHandler.readProducts(CSV_FILE);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not read " + CSV_FILE + ": " + e.getMessage());
        }

        // 2. Read full chronological log history grouped by product
        Map<String, List<LogEntry>> historyLogs = loadAllLogsGrouped();

        // 3. Scan available screenshots (maps normalized name to filename)
        Map<String, String> latestScreenshots = loadLatestScreenshotFilenames();

        List<ProductResponse> responses = new ArrayList<>();
        Set<String> processedNames = new HashSet<>();

        // Build list starting from configured products
        for (Product product : configuredProducts) {
            String name = product.getName();
            processedNames.add(name.toLowerCase());
            String id = generateId(name);

            List<LogEntry> entries = findMatchingHistory(name, historyLogs);
            String screenshotFilename = findMatchingScreenshotFilename(name, latestScreenshots);
            String screenshotUrl = screenshotFilename != null ? "/api/screenshots/" + screenshotFilename : null;

            String status = "NOT CHECKED";
            Double currentPrice = null;
            Double previousPrice = null;
            Double priceChange = null;
            String lastChecked = null;

            if (entries != null && !entries.isEmpty()) {
                LogEntry latest = entries.get(entries.size() - 1);
                status = latest.status;
                currentPrice = parsePrice(latest.priceStr);
                lastChecked = formatTimestamp(latest.timestamp);

                // Find previous price from earlier historical checks
                if (currentPrice != null) {
                    for (int i = entries.size() - 2; i >= 0; i--) {
                        Double prev = parsePrice(entries.get(i).priceStr);
                        if (prev != null) {
                            previousPrice = prev;
                            priceChange = Math.round((currentPrice - previousPrice) * 100.0) / 100.0;
                            break;
                        }
                    }
                }
            }

            String url = product.hasUrl() ? product.getUrl() : null;
            responses.add(new ProductResponse(id, name, status, currentPrice, previousPrice, priceChange, lastChecked, url, screenshotUrl));
        }

        return responses;
    }

    public SummaryResponse getSummary() {
        List<ProductResponse> products = getProducts();

        int total = products.size();
        int inStock = 0;
        int outOfStock = 0;
        int errors = 0;
        int priceDrops = 0;
        String latestCheck = null;

        for (ProductResponse p : products) {
            if (p.getStatus() != null) {
                String s = p.getStatus().toUpperCase();
                if (s.contains("IN STOCK")) {
                    inStock++;
                } else if (s.contains("OUT OF STOCK")) {
                    outOfStock++;
                } else if (s.contains("ERROR") || s.contains("UNAVAILABLE") || s.contains("NOT CHECKED") || s.contains("404") || s.contains("NOT FOUND")) {
                    errors++;
                }
            } else {
                errors++;
            }

            if (p.getPriceChange() != null && p.getPriceChange() < 0) {
                priceDrops++;
            }

            if (p.getLastChecked() != null) {
                if (latestCheck == null || p.getLastChecked().compareTo(latestCheck) > 0) {
                    latestCheck = p.getLastChecked();
                }
            }
        }

        return new SummaryResponse(total, inStock, outOfStock, errors, priceDrops, latestCheck);
    }

    public synchronized ProductResponse addProduct(ProductRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Product data is required");
        }
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        String validUrl = validateAndNormalizeUrl(request.getUrl());

        List<Product> products = new ArrayList<>();
        Path path = Path.of(CSV_FILE);
        if (Files.exists(path)) {
            products = CsvHandler.readProducts(CSV_FILE);
        }

        // Duplicate URL prevention
        String normUrl = normalizeUrlForComparison(validUrl);
        for (Product p : products) {
            if (normalizeUrlForComparison(p.getUrl()).equals(normUrl)) {
                throw new IllegalStateException("A product with this URL is already being monitored");
            }
        }

        Product newProduct = new Product(name, validUrl);
        products.add(newProduct);
        CsvHandler.writeProducts(products, CSV_FILE);

        String id = generateId(name);
        return new ProductResponse(id, name, "NOT CHECKED", null, null, null, null, validUrl, null);
    }

    public synchronized ProductResponse updateProduct(String id, ProductRequest request) throws IOException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Product data is required");
        }
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        String validUrl = validateAndNormalizeUrl(request.getUrl());

        List<Product> products = CsvHandler.readProducts(CSV_FILE);
        int targetIndex = -1;
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (generateId(p.getName()).equalsIgnoreCase(id) || p.getName().equalsIgnoreCase(id)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            throw new NoSuchElementException("Product not found: " + id);
        }

        // Check duplicate URL against other products
        String normUrl = normalizeUrlForComparison(validUrl);
        for (int i = 0; i < products.size(); i++) {
            if (i != targetIndex && normalizeUrlForComparison(products.get(i).getUrl()).equals(normUrl)) {
                throw new IllegalStateException("Another product already has this URL");
            }
        }

        products.set(targetIndex, new Product(name, validUrl));
        CsvHandler.writeProducts(products, CSV_FILE);

        // Return updated product representation
        List<ProductResponse> all = getProducts();
        String newId = generateId(name);
        for (ProductResponse pr : all) {
            if (pr.getId().equalsIgnoreCase(newId)) {
                return pr;
            }
        }
        return new ProductResponse(newId, name, "NOT CHECKED", null, null, null, null, validUrl, null);
    }

    public synchronized boolean deleteProduct(String id) throws IOException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        List<Product> products = CsvHandler.readProducts(CSV_FILE);
        boolean removed = products.removeIf(p -> generateId(p.getName()).equalsIgnoreCase(id) || p.getName().equalsIgnoreCase(id));
        if (removed) {
            CsvHandler.writeProducts(products, CSV_FILE);
            return true;
        }
        return false;
    }

    public List<HistoryEntryResponse> getAllHistory(int limit) {
        List<HistoryEntryResponse> list = new ArrayList<>();
        Path logPath = Path.of(LOG_FILE);
        if (!Files.exists(logPath)) {
            return list;
        }

        try {
            List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i);
                if (line == null || !line.contains("|")) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String timestamp = formatTimestamp(parts[0].trim());
                    String name = parts[1].trim();
                    String status = parts[2].trim();
                    String priceStr = parts.length > 3 ? parts[3].trim() : "";
                    Double price = parsePrice(priceStr);

                    list.add(new HistoryEntryResponse(timestamp, name, status, price, priceStr));
                    if (limit > 0 && list.size() >= limit) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Could not read " + LOG_FILE + ": " + e.getMessage());
        }

        return list;
    }

    private String validateAndNormalizeUrl(String url) {
        if (url == null || url.trim().isBlank()) {
            throw new IllegalArgumentException("Product URL is required");
        }
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }
        try {
            URI uri = new URI(trimmed);
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL host is invalid");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL syntax: " + e.getMessage());
        }
        return trimmed;
    }

    private String normalizeUrlForComparison(String url) {
        if (url == null) return "";
        String u = url.trim().toLowerCase();
        if (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    public List<PriceHistoryPoint> getProductHistory(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) {
            return Collections.emptyList();
        }

        Map<String, List<LogEntry>> historyLogs = loadAllLogsGrouped();

        // 1. Try matching by slug ID
        for (Map.Entry<String, List<LogEntry>> entry : historyLogs.entrySet()) {
            String prodName = entry.getKey();
            if (generateId(prodName).equalsIgnoreCase(idOrName) || prodName.equalsIgnoreCase(idOrName)) {
                return mapToHistoryPoints(entry.getValue());
            }
        }

        // 2. Also try matching with CSV products
        try {
            Path csvPath = Path.of(CSV_FILE);
            if (Files.exists(csvPath)) {
                List<Product> csvProducts = CsvHandler.readProducts(CSV_FILE);
                for (Product p : csvProducts) {
                    if (generateId(p.getName()).equalsIgnoreCase(idOrName) || p.getName().equalsIgnoreCase(idOrName)) {
                        List<LogEntry> entries = findMatchingHistory(p.getName(), historyLogs);
                        return mapToHistoryPoints(entries);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return Collections.emptyList();
    }

    private List<PriceHistoryPoint> mapToHistoryPoints(List<LogEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<PriceHistoryPoint> points = new ArrayList<>();
        for (LogEntry entry : entries) {
            points.add(new PriceHistoryPoint(
                    formatTimestamp(entry.timestamp),
                    parsePrice(entry.priceStr),
                    entry.status
            ));
        }
        return points;
    }

    private Map<String, List<LogEntry>> loadAllLogsGrouped() {
        Map<String, List<LogEntry>> map = new LinkedHashMap<>();
        Path logPath = Path.of(LOG_FILE);

        if (!Files.exists(logPath)) {
            return map;
        }

        try {
            List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || !line.contains("|")) continue;

                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String timestamp = parts[0].trim();
                    String name = parts[1].trim();
                    String status = parts[2].trim();
                    String priceStr = parts.length > 3 ? parts[3].trim() : "";

                    map.computeIfAbsent(name, k -> new ArrayList<>())
                       .add(new LogEntry(timestamp, status, priceStr));
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Could not read " + LOG_FILE + ": " + e.getMessage());
        }

        return map;
    }

    private Map<String, String> loadLatestScreenshotFilenames() {
        Map<String, String> map = new HashMap<>();
        File dir = new File(SCREENSHOTS_DIR);

        if (!dir.exists() || !dir.isDirectory()) {
            return map;
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null) {
            return map;
        }

        // Sort descending by filename timestamp so newest is encountered first
        Arrays.sort(files, (a, b) -> b.getName().compareTo(a.getName()));

        for (File file : files) {
            String filename = file.getName();
            int lastUnderscore = filename.lastIndexOf('_');
            if (lastUnderscore > 0) {
                String namePrefix = filename.substring(0, lastUnderscore).toLowerCase();
                map.putIfAbsent(namePrefix, filename);
            }
        }

        return map;
    }

    private List<LogEntry> findMatchingHistory(String productName, Map<String, List<LogEntry>> logs) {
        if (logs.containsKey(productName)) {
            return logs.get(productName);
        }
        for (Map.Entry<String, List<LogEntry>> entry : logs.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(productName)) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }

    private String findMatchingScreenshotFilename(String productName, Map<String, String> screenshots) {
        String key = productName.replace(" ", "_").toLowerCase();
        if (screenshots.containsKey(key)) {
            return screenshots.get(key);
        }
        for (Map.Entry<String, String> entry : screenshots.entrySet()) {
            if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static String generateId(String name) {
        if (name == null) return "unknown";
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "product" : slug;
    }

    private Double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isBlank() || priceStr.toUpperCase().contains("NOT FOUND")) {
            return null;
        }
        String clean = priceStr.replace("₹", "").replace(",", "").trim();
        try {
            return Double.valueOf(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatTimestamp(String raw) {
        if (raw == null || raw.isBlank()) return null;
        int dot = raw.indexOf('.');
        if (dot > 0) {
            return raw.substring(0, dot);
        }
        return raw;
    }
}
