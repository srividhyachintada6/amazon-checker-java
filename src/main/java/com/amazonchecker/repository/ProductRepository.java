package com.amazonchecker.repository;

import com.amazonchecker.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByActiveTrueOrderByCreatedAtDesc();

    List<ProductEntity> findByActiveTrue();

    List<ProductEntity> findByActiveTrueAndStore(com.amazonchecker.model.Store store);

    Optional<ProductEntity> findByUrl(String url);

    Optional<ProductEntity> findByNameIgnoreCase(String name);

    boolean existsByUrl(String url);

    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) = LOWER(:name) AND p.active = true")
    Optional<ProductEntity> findActiveByName(@Param("name") String name);

    @Query("SELECT p FROM ProductEntity p WHERE p.url = :url AND p.id <> :excludeId")
    Optional<ProductEntity> findByUrlAndIdNot(@Param("url") String url, @Param("excludeId") Long excludeId);
}
