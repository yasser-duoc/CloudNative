package com.pedidos360.audit.service;

import com.pedidos360.audit.domain.AuditEvent;
import com.pedidos360.audit.domain.WorkOrderEvent;
import com.pedidos360.audit.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditService {

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public void record(WorkOrderEvent event) {
        AuditEvent audit = new AuditEvent();
        audit.setEventId(event.eventId());
        audit.setEventType(event.eventType());
        audit.setWorkOrderId(event.workOrderId());
        audit.setStatus(event.status());
        audit.setActor(event.actor());
        audit.setEventTimestamp(event.timestamp());
        repository.save(audit);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> timeline(Long workOrderId) {
        return repository.findByWorkOrderIdOrderByEventTimestampAsc(workOrderId);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> findAll() {
        return repository.findAll();
    }
}
