package com.soar_be.domain.product.entity;

import com.soar_be.domain.ranking.entity.Ranking;
import com.soar_be.domain.store.entity.Store;
import com.soar_be.domain.suggestion.entity.Suggestion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "product",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "management_code"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "management_code", nullable = false, length = 100)
    private String managementCode;

    @Column(name = "registered_name", nullable = false, length = 300)
    private String registeredName;

    @Column(name = "actual_product_name", length = 300)
    private String actualProductName;

    @Column(name = "primary_keyword", nullable = false, length = 200)
    private String primaryKeyword;

    @Column(nullable = false, length = 50)
    private String marketplace = "스마트스토어";

    @Column(name = "registered_date")
    private LocalDate registeredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ranking> rankings = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Suggestion> suggestions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String registeredName, String actualProductName,
                       String primaryKeyword, String marketplace, LocalDate registeredDate) {
        if (registeredName != null) {
            this.registeredName = registeredName;
        }
        if (actualProductName != null) {
            this.actualProductName = actualProductName;
        }
        if (primaryKeyword != null) {
            this.primaryKeyword = primaryKeyword;
        }
        if (marketplace != null) {
            this.marketplace = marketplace;
        }
        if (registeredDate != null) {
            this.registeredDate = registeredDate;
        }
    }

    public void updateStatus(ProductStatus status) {
        this.status = status;
    }
}