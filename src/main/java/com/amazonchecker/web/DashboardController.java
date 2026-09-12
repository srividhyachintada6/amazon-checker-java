package com.amazonchecker.web;

import com.amazonchecker.scheduler.SchedulerRunner;
import com.amazonchecker.web.dto.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.amazonchecker.entity.ProductEntity;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final ProductService productService;
    private final SchedulerRunner schedulerRunner;

    public DashboardController(ProductService productService, SchedulerRunner schedulerRunner) {
        this.productService = productService;
        this.schedulerRunner = schedulerRunner;
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductResponse> getProducts() {
        return productService.getProducts();
    }

    @GetMapping(value = "/products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") String id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping(value = "/products/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SearchResultResponse> searchProducts(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "store", defaultValue = "all") String store) {
        if (query == null || query.trim().isBlank()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        return productService.searchProducts(query, store);
    }

    @GetMapping(value = "/comparison", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PriceComparisonResponse> getPriceComparisons() {
        return productService.getMonitoredPriceComparisons();
    }

    @GetMapping(value = "/comparison/live", produces = MediaType.APPLICATION_JSON_VALUE)
    public PriceComparisonResponse getLiveComparison(@RequestParam("query") String query) {
        if (query == null || query.trim().isBlank()) {
            throw new IllegalArgumentException("Comparison query cannot be empty");
        }
        return productService.compareLivePrices(query);
    }

    @PostMapping(value = "/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest request) throws IOException {
        ProductResponse created = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/products/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") String id, @RequestBody ProductRequest request) throws IOException {
        ProductResponse updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/products/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable("id") String id) throws IOException {
        boolean removed = productService.deleteProduct(id);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", "Product not found: " + id
            ));
        }
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Product removed successfully"
        ));
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HistoryEntryResponse> getHistory(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        return productService.getAllHistory(limit);
    }

    @GetMapping(value = "/products/{id}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PriceHistoryPoint>> getProductHistory(@PathVariable("id") String id) {
        List<PriceHistoryPoint> history = productService.getProductHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryResponse getSummary() {
        return productService.getSummary();
    }

    /**
     * Controlled and safe screenshot retrieval endpoint.
     * Restricts strictly to valid images within the screenshots directory and prevents path traversal.
     */
    @GetMapping("/screenshots/{filename:.+}")
    public ResponseEntity<Resource> getScreenshot(@PathVariable("filename") String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\") || filename.contains("%")) {
            return ResponseEntity.badRequest().build();
        }

        String lower = filename.toLowerCase();
        if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) {
            return ResponseEntity.badRequest().build();
        }

        Path screenshotDir = Path.of("screenshots").toAbsolutePath().normalize();
        Path filePath = screenshotDir.resolve(filename).normalize();

        // Enforce boundary check: strictly under screenshots/
        if (!filePath.startsWith(screenshotDir) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            MediaType mediaType = lower.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();

        boolean isChecking = schedulerRunner.isChecking();
        boolean isEnabled = schedulerRunner.isEnabled();

        String status;
        String message;

        if (isChecking) {
            status = "checking";
            message = "Checking Amazon products...";
        } else if (isEnabled) {
            status = "automatic monitoring";
            message = "Automatic monitoring active";
        } else {
            status = "ready";
            message = "Amazon checker is ready";
        }

        response.put("status", status);
        response.put("message", message);
        response.put("checking", isChecking);

        Map<String, Object> scheduler = new HashMap<>();
        scheduler.put("enabled", isEnabled);
        scheduler.put("intervalMinutes", schedulerRunner.getIntervalMinutes());
        scheduler.put("lastChecked", schedulerRunner.getFormattedLastCheckTime());
        scheduler.put("nextCheck", schedulerRunner.getFormattedNextCheckTime());
        scheduler.put("minutesUntilNextCheck", schedulerRunner.getMinutesUntilNextCheck());
        scheduler.put("secondsUntilNextCheck", schedulerRunner.getSecondsUntilNextCheck());

        response.put("scheduler", scheduler);
        response.put("nextCheckMinutes", schedulerRunner.getMinutesUntilNextCheck());
        response.put("scheduleIntervalMinutes", schedulerRunner.getIntervalMinutes());

        return response;
    }

    @PostMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> checkProducts() {
        boolean started = schedulerRunner.triggerManualCheck();
        if (!started) {
            return Map.of(
                    "status", "checking",
                    "message", "A check is already running"
            );
        }

        return Map.of(
                "status", "started",
                "message", "Amazon checking started"
        );
    }

    @GetMapping("/log")
    public String getLog() {
        try {
            Path logFile = Path.of("data", "availability_log.txt");
            if (!Files.exists(logFile)) {
                return "";
            }
            return Files.readString(logFile);
        } catch (IOException e) {
            return "Unable to read log: " + e.getMessage();
        }
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            List<ProductEntity> active = productService.getActiveProducts();
            health.put("database", "CONNECTED");
            health.put("activeProducts", active.size());
        } catch (Exception e) {
            health.put("database", "DISCONNECTED: " + e.getMessage());
            health.put("status", "DOWN");
        }

        health.put("schedulerEnabled", schedulerRunner.isEnabled());
        health.put("schedulerIntervalMinutes", schedulerRunner.getIntervalMinutes());
        health.put("isChecking", schedulerRunner.isChecking());
        health.put("lastCheck", schedulerRunner.getFormattedLastCheckTime());
        health.put("nextCheck", schedulerRunner.getFormattedNextCheckTime());

        HttpStatus status = "UP".equals(health.get("status")) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", "error",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "error",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", "error",
                "message", "Database constraint violation: duplicate record or invalid relationship"
        ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "error",
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "An error occurred while processing your request"
        ));
    }
}