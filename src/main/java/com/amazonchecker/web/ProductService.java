package com.amazonchecker.web;

import com.amazonchecker.entity.MonitoringResultEntity;
import com.amazonchecker.entity.ProductEntity;
import com.amazonchecker.repository.MonitoringResultRepository;
import com.amazonchecker.repository.ProductRepository;
import com.amazonchecker.scraper.SearchScraper;
import com.amazonchecker.utils.CsvHandler;
import com.amazonchecker.utils.LogWriter;
import com.amazonchecker.utils.Product;
import com.amazonchecker.web.dto.*;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProductService {

    private static final String CSV_FILE = "data/products.csv";
    private static final String LOG_FILE = "data/availability_log.txt";
    private static final String SCREENSHOTS_DIR = "screenshots";

    private final ProductRepository productRepository;
    private final MonitoringResultRepository monitoringResultRepository;

    public ProductService(ProductRepository productRepository, MonitoringResultRepository monitoringResultRepository) {
        this.productRepository = productRepository;
        this.monitoringResultRepository = monitoringResultRepository;
    }

    /**
     * First-boot Migration: If the database is empty, seed from existing data/products.csv and availability_log.txt.
     * Triggered on ApplicationReadyEvent once Hibernate DDL table creation has completed.
     */
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    @Transactional
    public void migrateFromFilesIfEmpty() {
        try {
            if (productRepository.count() > 0) {
                return;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not check product count on startup: " + e.getMessage());
            return;
        }

        System.out.println("🔄 Initializing database from data/products.csv and data/availability_log.txt...");

        List<Product> csvProducts = new ArrayList<>();
        try {
            Path csvPath = Path.of(CSV_FILE);
            if (Files.exists(csvPath)) {
                csvProducts = CsvHandler.readProducts(CSV_FILE);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not read " + CSV_FILE + " during migration: " + e.getMessage());
        }

        Map<String, ProductEntity> savedByName = new HashMap<>();
        for (Product p : csvProducts) {
            if (p.getUrl() == null || p.getUrl().isBlank()) continue;
            try {
                ProductEntity entity = new ProductEntity(p.getName(), p.getUrl().trim());
                entity = productRepository.save(entity);
                savedByName.put(p.getName().toLowerCase(), entity);
            } catch (Exception e) {
                System.err.println("⚠️ Migration error saving product " + p.getName() + ": " + e.getMessage());
            }
        }

        // Migrate historical availability log
        try {
            Path logPath = Path.of(LOG_FILE);
            if (Files.exists(logPath)) {
                List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
                int migratedCount = 0;
                for (String line : lines) {
                    if (line == null || !line.contains("|")) continue;
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        String tsStr = parts[0].trim();
                        String prodName = parts[1].trim();
                        String status = parts[2].trim();
                        String priceStr = parts.length > 3 ? parts[3].trim() : "";
                        Double price = parsePrice(priceStr);

                        ProductEntity entity = savedByName.get(prodName.toLowerCase());
                        if (entity != null) {
                            LocalDateTime checkedAt = parseTimestamp(tsStr);
                            monitoringResultRepository.save(new MonitoringResultEntity(
                                    entity,
                                    checkedAt != null ? checkedAt : LocalDateTime.now(),
                                    price,
                                    status,
                                    "Migrated historical check",
                                    null
                            ));
                            migratedCount++;
                        }
                    }
                }
                System.out.println("🎉 Migration complete: " + savedByName.size() + " products and " + migratedCount + " monitoring logs imported to database.");
            }
        } catch (Exception e) {
            System.err.println("⚠️ History migration warning: " + e.getMessage());
        }
    }

    public List<ProductEntity> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<ProductResponse> getProducts() {
        List<ProductEntity> entities = productRepository.findByActiveTrueOrderByCreatedAtDesc();
        Map<String, String> latestScreenshots = loadLatestScreenshotFilenames();

        List<ProductResponse> responses = new ArrayList<>();
        for (ProductEntity product : entities) {
            responses.add(mapToProductResponse(product, latestScreenshots));
        }

        return responses;
    }

    public ProductResponse getProductById(String idOrSlug) {
        ProductEntity product = findProductByIdOrSlug(idOrSlug)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + idOrSlug));
        return mapToProductResponse(product, loadLatestScreenshotFilenames());
    }

    private ProductResponse mapToProductResponse(ProductEntity product, Map<String, String> latestScreenshots) {
        List<MonitoringResultEntity> results = monitoringResultRepository.findByProductIdOrderByCheckedAtDesc(product.getId());

        String status = "NOT CHECKED";
        Double currentPrice = null;
        Double previousPrice = null;
        Double priceChange = null;
        String lastChecked = null;
        String screenshotUrl = null;

        if (results != null && !results.isEmpty()) {
            MonitoringResultEntity latest = results.get(0);
            status = latest.getAvailability();
            currentPrice = latest.getPrice();
            lastChecked = formatDateTime(latest.getCheckedAt());
            screenshotUrl = latest.getScreenshotUrl();

            if (currentPrice != null) {
                for (int i = 1; i < results.size(); i++) {
                    Double prev = results.get(i).getPrice();
                    if (prev != null) {
                        previousPrice = prev;
                        priceChange = Math.round((currentPrice - previousPrice) * 100.0) / 100.0;
                        break;
                    }
                }
            }
        }

        if (screenshotUrl == null) {
            String screenshotFilename = findMatchingScreenshotFilename(product.getName(), latestScreenshots);
            if (screenshotFilename != null) {
                screenshotUrl = "/api/screenshots/" + screenshotFilename;
            }
        }

        return new ProductResponse(
                String.valueOf(product.getId()),
                product.getName(),
                status,
                currentPrice,
                previousPrice,
                priceChange,
                lastChecked,
                product.getUrl(),
                screenshotUrl
        );
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

    @Transactional
    public ProductResponse addProduct(ProductRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Product data is required");
        }
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        String validUrl = validateAndNormalizeUrl(request.getUrl());

        // Check duplicate URL against database
        String normUrl = normalizeUrlForComparison(validUrl);
        List<ProductEntity> existingProducts = productRepository.findAll();
        for (ProductEntity p : existingProducts) {
            if (normalizeUrlForComparison(p.getUrl()).equals(normUrl)) {
                throw new IllegalStateException("A product with this URL is already being monitored");
            }
        }

        ProductEntity entity = new ProductEntity(name, validUrl);
        entity = productRepository.save(entity);

        syncCsv();

        return new ProductResponse(
                String.valueOf(entity.getId()),
                entity.getName(),
                "NOT CHECKED",
                null,
                null,
                null,
                null,
                entity.getUrl(),
                null
        );
    }

    @Transactional
    public ProductResponse updateProduct(String idOrSlug, ProductRequest request) throws IOException {
        ProductEntity entity = findProductByIdOrSlug(idOrSlug)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + idOrSlug));

        if (request == null) {
            throw new IllegalArgumentException("Product data is required");
        }
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        String validUrl = validateAndNormalizeUrl(request.getUrl());

        // Check duplicate URL against other products
        String normUrl = normalizeUrlForComparison(validUrl);
        List<ProductEntity> existingProducts = productRepository.findAll();
        for (ProductEntity p : existingProducts) {
            if (!p.getId().equals(entity.getId()) && normalizeUrlForComparison(p.getUrl()).equals(normUrl)) {
                throw new IllegalStateException("Another product already has this URL");
            }
        }

        entity.setName(name);
        entity.setUrl(validUrl);
        entity = productRepository.save(entity);

        syncCsv();

        return mapToProductResponse(entity, loadLatestScreenshotFilenames());
    }

    @Transactional
    public boolean deleteProduct(String idOrSlug) throws IOException {
        Optional<ProductEntity> optional = findProductByIdOrSlug(idOrSlug);
        if (optional.isEmpty()) {
            return false;
        }

        ProductEntity entity = optional.get();
        monitoringResultRepository.deleteByProductId(entity.getId());
        productRepository.delete(entity);

        syncCsv();
        return true;
    }

    @Transactional
    public void recordCheckResult(ProductEntity product, String availability, String priceStr, String screenshotUrl) {
        Double price = parsePrice(priceStr);
        MonitoringResultEntity result = new MonitoringResultEntity(
                product,
                LocalDateTime.now(),
                price,
                availability != null ? availability : "UNKNOWN",
                "Periodic Check",
                screenshotUrl
        );
        monitoringResultRepository.save(result);

        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        // Dual-log to availability_log.txt for backward compatibility
        LogWriter.logResult(product.getName(), availability, priceStr);
    }

    public List<PriceHistoryPoint> getProductHistory(String idOrSlug) {
        Optional<ProductEntity> optional = findProductByIdOrSlug(idOrSlug);
        if (optional.isEmpty()) {
            return Collections.emptyList();
        }

        List<MonitoringResultEntity> results = monitoringResultRepository.findByProductIdOrderByCheckedAtAsc(optional.get().getId());
        List<PriceHistoryPoint> points = new ArrayList<>();
        for (MonitoringResultEntity r : results) {
            points.add(new PriceHistoryPoint(
                    formatDateTime(r.getCheckedAt()),
                    r.getPrice(),
                    r.getAvailability()
            ));
        }
        return points;
    }

    public List<HistoryEntryResponse> getAllHistory(int limit) {
        int max = limit > 0 ? limit : 50;
        List<MonitoringResultEntity> results = monitoringResultRepository.findAllByOrderByCheckedAtDesc(PageRequest.of(0, max));
        List<HistoryEntryResponse> list = new ArrayList<>();
        for (MonitoringResultEntity r : results) {
            String priceStr = r.getPrice() != null ? "₹" + String.format(Locale.ENGLISH, "%,.2f", r.getPrice()) : "Price Not Available";
            list.add(new HistoryEntryResponse(
                    formatDateTime(r.getCheckedAt()),
                    r.getProduct() != null ? r.getProduct().getName() : "Unknown Product",
                    r.getAvailability(),
                    r.getPrice(),
                    priceStr
            ));
        }
        return list;
    }

    public Optional<ProductEntity> findProductByIdOrSlug(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            return Optional.empty();
        }
        String trimmed = idOrSlug.trim();
        try {
            long numericId = Long.parseLong(trimmed);
            Optional<ProductEntity> byId = productRepository.findById(numericId);
            if (byId.isPresent()) {
                return byId;
            }
        } catch (NumberFormatException ignored) {
        }

        List<ProductEntity> all = productRepository.findAll();
        for (ProductEntity p : all) {
            if (generateId(p.getName()).equalsIgnoreCase(trimmed) || p.getName().equalsIgnoreCase(trimmed)) {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }

    private synchronized void syncCsv() {
        try {
            List<ProductEntity> active = productRepository.findByActiveTrue();
            List<Product> csvList = new ArrayList<>();
            for (ProductEntity p : active) {
                csvList.add(new Product(p.getName(), p.getUrl()));
            }
            CsvHandler.writeProducts(csvList, CSV_FILE);
        } catch (Exception e) {
            System.err.println("⚠️ Could not sync products.csv: " + e.getMessage());
        }
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

    public static Double parsePrice(String priceStr) {
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

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private LocalDateTime parseTimestamp(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            String clean = raw.trim();
            if (clean.contains("T")) {
                int dot = clean.indexOf('.');
                if (dot > 0) clean = clean.substring(0, dot);
                return LocalDateTime.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                int dot = clean.indexOf('.');
                if (dot > 0) clean = clean.substring(0, dot);
                return LocalDateTime.parse(clean, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public List<SearchResultResponse> searchProducts(String query) {
        if (query == null || query.trim().isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return SearchScraper.searchProducts(query.trim());
    }
}
