package com.amazonchecker.repository;

import com.amazonchecker.entity.MonitoringResultEntity;
import com.amazonchecker.entity.ProductEntity;
import com.amazonchecker.web.WebApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WebApplication.class)
@ActiveProfiles("test")
@Transactional
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MonitoringResultRepository monitoringResultRepository;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        monitoringResultRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve a product entity by ID and URL")
    void testSaveAndFindProduct() {
        ProductEntity product = new ProductEntity("Test Mechanical Keyboard", "https://www.amazon.in/dp/B0TEST1234");
        ProductEntity saved = productRepository.save(product);

        assertNotNull(saved.getId(), "Product ID should be generated");
        assertTrue(saved.isActive(), "Product should be active by default");
        assertNotNull(saved.getCreatedAt(), "CreatedAt timestamp should be generated");

        Optional<ProductEntity> found = productRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Mechanical Keyboard", found.get().getName());

        Optional<ProductEntity> byUrl = productRepository.findByUrl("https://www.amazon.in/dp/B0TEST1234");
        assertTrue(byUrl.isPresent());
        assertEquals(saved.getId(), byUrl.get().getId());
    }

    @Test
    @DisplayName("Should check existence and retrieve active products in descending order")
    void testActiveProductsQuery() {
        ProductEntity p1 = productRepository.save(new ProductEntity("Product 1", "https://www.amazon.in/dp/B001"));
        ProductEntity p2 = productRepository.save(new ProductEntity("Product 2", "https://www.amazon.in/dp/B002"));

        p2.setActive(false);
        productRepository.save(p2);

        List<ProductEntity> activeList = productRepository.findByActiveTrueOrderByCreatedAtDesc();
        assertEquals(1, activeList.size());
        assertEquals("Product 1", activeList.get(0).getName());

        assertTrue(productRepository.existsByUrl("https://www.amazon.in/dp/B001"));
        assertFalse(productRepository.existsByUrl("https://www.amazon.in/dp/NONEXISTENT"));
    }

    @Test
    @DisplayName("Should save monitoring results for product and retrieve ordered history")
    void testSaveMonitoringResults() {
        ProductEntity product = productRepository.save(new ProductEntity("Smart Watch", "https://www.amazon.in/dp/B0WATCH"));

        MonitoringResultEntity r1 = new MonitoringResultEntity(
                product,
                LocalDateTime.now().minusHours(2),
                2999.0,
                "IN STOCK",
                "Periodic Check",
                "/api/screenshots/watch1.png"
        );
        monitoringResultRepository.save(r1);

        MonitoringResultEntity r2 = new MonitoringResultEntity(
                product,
                LocalDateTime.now().minusHours(1),
                2799.0,
                "IN STOCK",
                "Periodic Check",
                "/api/screenshots/watch2.png"
        );
        monitoringResultRepository.save(r2);

        List<MonitoringResultEntity> results = monitoringResultRepository.findByProductIdOrderByCheckedAtDesc(product.getId());
        assertEquals(2, results.size());
        assertEquals(2799.0, results.get(0).getPrice());
        assertEquals(2999.0, results.get(1).getPrice());

        Optional<MonitoringResultEntity> latest = monitoringResultRepository.findLatestByProductId(product.getId());
        assertTrue(latest.isPresent());
        assertEquals(2799.0, latest.get().getPrice());
    }

    @Test
    @DisplayName("Should delete monitoring results when clearing product history")
    void testDeleteMonitoringResultsByProduct() {
        ProductEntity product = productRepository.save(new ProductEntity("Mouse Pad", "https://www.amazon.in/dp/B0PAD"));
        monitoringResultRepository.save(new MonitoringResultEntity(product, LocalDateTime.now(), 299.0, "IN STOCK", "Check", null));

        monitoringResultRepository.deleteByProductId(product.getId());
        List<MonitoringResultEntity> results = monitoringResultRepository.findByProductIdOrderByCheckedAtDesc(product.getId());
        assertTrue(results.isEmpty());
    }
}
