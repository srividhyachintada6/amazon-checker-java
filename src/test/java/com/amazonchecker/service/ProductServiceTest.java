package com.amazonchecker.service;

import com.amazonchecker.entity.MonitoringResultEntity;
import com.amazonchecker.entity.ProductEntity;
import com.amazonchecker.repository.MonitoringResultRepository;
import com.amazonchecker.repository.ProductRepository;
import com.amazonchecker.web.ProductService;
import com.amazonchecker.web.WebApplication;
import com.amazonchecker.web.dto.ProductRequest;
import com.amazonchecker.web.dto.ProductResponse;
import com.amazonchecker.web.dto.SummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WebApplication.class)
@ActiveProfiles("test")
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MonitoringResultRepository monitoringResultRepository;

    @BeforeEach
    void setup() {
        monitoringResultRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully add a valid product")
    void testAddProductSuccess() throws IOException {
        ProductRequest req = new ProductRequest("Sony Headphones WH-1000XM5", "https://www.amazon.in/dp/B09XS7JWHH");
        ProductResponse res = productService.addProduct(req);

        assertNotNull(res.getId());
        assertEquals("Sony Headphones WH-1000XM5", res.getName());
        assertEquals("https://www.amazon.in/dp/B09XS7JWHH", res.getUrl());
        assertEquals("NOT CHECKED", res.getStatus());
        assertNull(res.getPrice());
    }

    @Test
    @DisplayName("Should reject product with blank name")
    void testAddProductBlankName() {
        ProductRequest req = new ProductRequest("   ", "https://www.amazon.in/dp/B09XS7JWHH");
        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(req));
    }

    @Test
    @DisplayName("Should reject product with invalid URL scheme")
    void testAddProductInvalidUrl() {
        ProductRequest req = new ProductRequest("Invalid URL Product", "ftp://invalid-url.com");
        assertThrows(IllegalArgumentException.class, () -> productService.addProduct(req));
    }

    @Test
    @DisplayName("Should prevent adding duplicate product URLs")
    void testPreventDuplicateUrl() throws IOException {
        ProductRequest req1 = new ProductRequest("Product Alpha", "https://www.amazon.in/dp/B0DUP12345");
        productService.addProduct(req1);

        ProductRequest req2 = new ProductRequest("Product Alpha Duplicate", "https://www.amazon.in/dp/B0DUP12345/");
        assertThrows(IllegalStateException.class, () -> productService.addProduct(req2));
    }

    @Test
    @DisplayName("Should calculate price drops and summary analytics correctly")
    void testSummaryAnalyticsAndPriceChange() {
        // Product 1: In Stock with price drop
        ProductEntity p1 = productRepository.save(new ProductEntity("Product One", "https://www.amazon.in/dp/B0P1"));
        monitoringResultRepository.save(new MonitoringResultEntity(p1, LocalDateTime.now().minusDays(1), 1000.0, "IN STOCK", "Old", null));
        monitoringResultRepository.save(new MonitoringResultEntity(p1, LocalDateTime.now(), 850.0, "IN STOCK", "New", null));

        // Product 2: Out of Stock
        ProductEntity p2 = productRepository.save(new ProductEntity("Product Two", "https://www.amazon.in/dp/B0P2"));
        monitoringResultRepository.save(new MonitoringResultEntity(p2, LocalDateTime.now(), null, "OUT OF STOCK", "New", null));

        SummaryResponse summary = productService.getSummary();
        assertEquals(2, summary.getTotalProducts());
        assertEquals(1, summary.getInStock());
        assertEquals(1, summary.getOutOfStock());
        assertEquals(0, summary.getErrors());
        assertEquals(1, summary.getPriceDrops(), "Should detect 1 price drop (1000 -> 850)");

        ProductResponse p1Resp = productService.getProductById(String.valueOf(p1.getId()));
        assertEquals(850.0, p1Resp.getPrice());
        assertEquals(1000.0, p1Resp.getPreviousPrice());
        assertEquals(-150.0, p1Resp.getPriceChange());
    }

    @Test
    @DisplayName("Should update product name and URL successfully")
    void testUpdateProduct() throws IOException {
        ProductResponse created = productService.addProduct(new ProductRequest("Old Name", "https://www.amazon.in/dp/B0OLD"));
        ProductResponse updated = productService.updateProduct(created.getId(), new ProductRequest("New Name", "https://www.amazon.in/dp/B0NEW"));

        assertEquals("New Name", updated.getName());
        assertEquals("https://www.amazon.in/dp/B0NEW", updated.getUrl());
    }

    @Test
    @DisplayName("Should delete product and its associated monitoring results")
    void testDeleteProduct() throws IOException {
        ProductResponse created = productService.addProduct(new ProductRequest("To Delete", "https://www.amazon.in/dp/B0DEL"));
        ProductEntity entity = productRepository.findById(Long.parseLong(created.getId())).orElseThrow();
        monitoringResultRepository.save(new MonitoringResultEntity(entity, LocalDateTime.now(), 499.0, "IN STOCK", "Log", null));

        boolean deleted = productService.deleteProduct(created.getId());
        assertTrue(deleted);
        assertFalse(productRepository.existsById(entity.getId()));
        assertTrue(monitoringResultRepository.findByProductIdOrderByCheckedAtDesc(entity.getId()).isEmpty());
    }

    @Test
    @DisplayName("Should parse prices accurately")
    void testPriceParser() {
        assertEquals(369.0, ProductService.parsePrice("₹369.00"));
        assertEquals(12999.50, ProductService.parsePrice("₹ 12,999.50"));
        assertEquals(500.0, ProductService.parsePrice("500"));
        assertNull(ProductService.parsePrice("Price not found"));
        assertNull(ProductService.parsePrice(null));
        assertNull(ProductService.parsePrice(""));
    }

    @Test
    @DisplayName("Should generate slug ID properly")
    void testGenerateSlug() {
        assertEquals("amazon-basics-hdmi-cable", ProductService.generateId("Amazon Basics HDMI Cable"));
        assertEquals("boat-rockerz-450", ProductService.generateId("boAt Rockerz 450!"));
        assertEquals("unknown", ProductService.generateId(null));
    }
}
