package com.soar_be.domain.store.repository;

import com.soar_be.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {
    List<Store> findAllByUserId(Long userId);

    Optional<Store> findById(Long id);

    boolean existsByName(String name);
}
