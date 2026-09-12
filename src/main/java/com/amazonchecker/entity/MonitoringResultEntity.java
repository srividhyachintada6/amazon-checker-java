package com.amazonchecker.entity;

import com.amazonchecker.model.Store;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a historical monitoring audit result across stores.
 */
@Entity
@Table(name = "monitoring_results", indexes = {
        @Index(name = "idx_result_product_id", columnList = "product_id"),
        @Index(name = "idx_result_checked_at", columnList = "checked_at"),
        @Index(name = "idx_result_store", columnList = "store")
})
public class MonitoringResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(name = "store", nullable = false, length = 32, columnDefinition = "varchar(32) default 'AMAZON'")
    private Store store = Store.AMAZON;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "price")
    private Double price;

    @Column(name = "availability", length = 64, nullable = false)
    private String availability;

    @Column(name = "status_message", length = 512)
    private String statusMessage;

    @Column(name = "screenshot_url", length = 512)
    private String screenshotUrl;

    public MonitoringResultEntity() {
    }

    public MonitoringResultEntity(ProductEntity product, LocalDateTime checkedAt, Double price,
                                  String availability, String statusMessage, String screenshotUrl) {
        this.product = product;
        this.store = product != null && product.getStore() != null ? product.getStore() : Store.AMAZON;
        this.checkedAt = checkedAt != null ? checkedAt : LocalDateTime.now();
        this.price = price;
        this.availability = availability;
        this.statusMessage = statusMessage;
        this.screenshotUrl = screenshotUrl;
    }

    public MonitoringResultEntity(ProductEntity product, Store store, LocalDateTime checkedAt, Double price,
                                  String availability, String statusMessage, String screenshotUrl) {
        this.product = product;
        this.store = store != null ? store : (product != null && product.getStore() != null ? product.getStore() : Store.AMAZON);
        this.checkedAt = checkedAt != null ? checkedAt : LocalDateTime.now();
        this.price = price;
        this.availability = availability;
        this.statusMessage = statusMessage;
        this.screenshotUrl = screenshotUrl;
    }

    @PrePersist
    protected void onCreate() {
        if (checkedAt == null) {
            checkedAt = LocalDateTime.now();
        }
        if (store == null) {
            if (product != null && product.getStore() != null) {
                store = product.getStore();
            } else {
                store = Store.AMAZON;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getScreenshotUrl() {
        return screenshotUrl;
    }

    public void setScreenshotUrl(String screenshotUrl) {
        this.screenshotUrl = screenshotUrl;
    }
}
