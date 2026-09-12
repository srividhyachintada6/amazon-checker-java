package com.amazonchecker.repository;

import com.amazonchecker.entity.MonitoringResultEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoringResultRepository extends JpaRepository<MonitoringResultEntity, Long> {

    List<MonitoringResultEntity> findByProductIdOrderByCheckedAtAsc(Long productId);

    List<MonitoringResultEntity> findByProductIdOrderByCheckedAtDesc(Long productId);

    List<MonitoringResultEntity> findAllByOrderByCheckedAtDesc(Pageable pageable);

    @Query("SELECT r FROM MonitoringResultEntity r WHERE r.product.id = :productId ORDER BY r.checkedAt DESC LIMIT 1")
    Optional<MonitoringResultEntity> findLatestByProductId(@Param("productId") Long productId);

    void deleteByProductId(Long productId);
}
