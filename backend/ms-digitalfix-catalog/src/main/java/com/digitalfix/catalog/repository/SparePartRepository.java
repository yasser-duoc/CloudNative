package com.digitalfix.catalog.repository;

import com.digitalfix.catalog.domain.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    List<SparePart> findByActiveTrue();

    Optional<SparePart> findBySku(String sku);
}
