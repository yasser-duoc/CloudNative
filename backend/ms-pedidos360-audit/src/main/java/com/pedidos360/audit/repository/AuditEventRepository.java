package com.pedidos360.audit.repository;

import com.pedidos360.audit.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByWorkOrderIdOrderByEventTimestampAsc(Long workOrderId);
}
