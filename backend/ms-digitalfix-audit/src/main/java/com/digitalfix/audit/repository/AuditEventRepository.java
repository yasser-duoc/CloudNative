package com.digitalfix.audit.repository;

import com.digitalfix.audit.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByWorkOrderIdOrderByEventTimestampAsc(Long workOrderId);
}
