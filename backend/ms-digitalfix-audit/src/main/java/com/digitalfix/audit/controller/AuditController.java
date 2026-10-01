package com.digitalfix.audit.controller;

import com.digitalfix.audit.domain.AuditEvent;
import com.digitalfix.audit.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping("/internal/events")
    @ResponseStatus(HttpStatus.CREATED)
    public void record(@RequestBody WorkOrderEvent event) {
        auditService.record(event);
    }

    @GetMapping("/workorders/{workOrderId}")
    public List<AuditEvent> timeline(@PathVariable Long workOrderId) {
        return auditService.timeline(workOrderId);
    }
}
