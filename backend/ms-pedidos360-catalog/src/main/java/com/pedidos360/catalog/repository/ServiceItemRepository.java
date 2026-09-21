package com.pedidos360.catalog.repository;

import com.pedidos360.catalog.domain.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findByActiveTrue();
}
