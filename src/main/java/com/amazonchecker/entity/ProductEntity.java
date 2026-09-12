package com.amazonchecker.entity;

import com.amazonchecker.model.Store;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a monitored product across supported stores (Amazon, Flipkart, etc.).
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_active", columnList = "active"),
        @Index(name = "idx_product_url", columnList = "amazon_url"),
        @Index(name = "idx_product_store", columnList = "store")
})
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amazon_url", nullable = false, length = 2048, unique = true)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "store", nullable = false, length = 32, columnDefinition = "varchar(32) default 'AMAZON'")
    private Store store = Store.AMAZON;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ProductEntity() {
    }

    public ProductEntity(String name, String url) {
        this.name = name;
        this.url = url;
        this.store = Store.AMAZON;
        this.active = true;
    }

    public ProductEntity(String name, String url, Store store) {
        this.name = name;
        this.url = url;
        this.store = store != null ? store : Store.AMAZON;
        this.active = true;
    }

    public ProductEntity(String name, String url, String imageUrl) {
        this(name, url, Store.AMAZON, imageUrl);
    }

    public ProductEntity(String name, String url, Store store, String imageUrl) {
        this.name = name;
        this.url = url;
        this.store = store != null ? store : Store.AMAZON;
        this.imageUrl = imageUrl;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (store == null) {
            try {
                store = Store.fromUrl(url);
            } catch (Exception e) {
                store = Store.AMAZON;
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
