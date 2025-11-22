package com.soar_be.domain.product.repository;

import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.entity.ProductStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByStoreId(Long storeId, Pageable pageable);

    Page<Product> findAllByStoreIdAndStatus(Long storeId, ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    boolean existsByStoreIdAndManagementCode(Long storeId, String managementCode);

    long countByStoreId(Long storeId);
}
