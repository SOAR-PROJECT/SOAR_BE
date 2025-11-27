package com.soar_be.domain.ranking.repository;

import com.soar_be.domain.ranking.entity.Ranking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    
    Optional<Ranking> findTopByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT r FROM Ranking r WHERE r.product.id = :productId " +
            "AND r.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY r.createdAt DESC")
    List<Ranking> findByProductIdAndCreatedAtBetween(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Ranking> findTop30ByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT r FROM Ranking r " +
            "JOIN r.product p " +
            "WHERE p.store.id = :storeId " +
            "AND DATE(r.createdAt) = DATE(:date) " +
            "ORDER BY r.createdAt DESC")
    List<Ranking> findByStoreIdAndDate(
            @Param("storeId") Long storeId,
            @Param("date") LocalDateTime date);

    @Query("SELECT r FROM Ranking r " +
            "WHERE r.id IN (" +
            "  SELECT MAX(r2.id) FROM Ranking r2 " +
            "  WHERE r2.product.store.id = :storeId " +
            "  GROUP BY r2.product.id" +
            ")")
    List<Ranking> findLatestByStoreId(@Param("storeId") Long storeId);

    long countByProductId(Long productId);
}