package com.digitalfix.audit.listener;

import com.digitalfix.audit.config.KafkaConfig;
import com.digitalfix.audit.domain.WorkOrderEvent;
import com.digitalfix.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(topics = KafkaConfig.AUDIT_TOPIC, groupId = KafkaConfig.AUDIT_GROUP_ID)
    public void onEvent(WorkOrderEvent event) {
        log.info("Evento de auditoría recibido: {} (orden {})", event.eventId(), event.workOrderId());
        auditService.record(event);
    }
}
