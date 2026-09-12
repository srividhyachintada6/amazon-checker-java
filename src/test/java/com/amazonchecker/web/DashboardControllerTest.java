package com.amazonchecker.web;

import com.amazonchecker.repository.MonitoringResultRepository;
import com.amazonchecker.repository.ProductRepository;
import com.amazonchecker.web.dto.ProductRequest;
import com.amazonchecker.web.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = WebApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("GET /api/products should return products list from database")
    void testGetProducts() throws Exception {
        productService.addProduct(new ProductRequest("Test Product Alpha", "https://www.amazon.in/dp/B0ALPHA123"));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name").value("Test Product Alpha"));
    }

    @Test
    @DisplayName("GET /api/products/{id} should return single product when exists")
    void testGetProductByIdSuccess() throws Exception {
        ProductResponse created = productService.addProduct(new ProductRequest("Specific Product", "https://www.amazon.in/dp/B0SPEC123"));

        mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Specific Product"));
    }

    @Test
    @DisplayName("GET /api/products/{id} should return 404 when product is not found")
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("GET /api/summary should return computed summary analytics")
    void testGetSummary() throws Exception {
        productService.addProduct(new ProductRequest("Summary Product", "https://www.amazon.in/dp/B0SUMM123"));

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProducts", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/health should return health report with status UP")
    void testGetHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("CONNECTED"))
                .andExpect(jsonPath("$.activeProducts", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/check should trigger check and return valid status")
    void testTriggerCheck() throws Exception {
        mockMvc.perform(post("/api/check"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", anyOf(is("started"), is("checking"))));
    }

    @Test
    @DisplayName("POST /api/products should reject invalid empty product name")
    void testAddProductValidation() throws Exception {
        ProductRequest invalidReq = new ProductRequest("", "https://www.amazon.in/dp/B0INV123");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Product name is required"));
    }
}
