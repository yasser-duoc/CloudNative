package com.pedidos360.audit.controller;

import com.pedidos360.audit.domain.AuditEvent;
import com.pedidos360.audit.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditEvent> findAll() {
        return auditService.findAll();
    }

    @GetMapping("/workorders/{workOrderId}")
    public List<AuditEvent> timeline(@PathVariable Long workOrderId) {
        return auditService.timeline(workOrderId);
    }
}
