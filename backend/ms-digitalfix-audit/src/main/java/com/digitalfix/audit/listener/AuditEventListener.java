package com.digitalfix.audit.listener;

import com.digitalfix.audit.config.KafkaConfig;
import com.digitalfix.audit.domain.WorkOrderEvent;
import com.digitalfix.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumidor del topic {@code workorder.audit}. Kafka es un log: cada evento
 * queda disponible para reprocesarse (replay) cambiando el offset del grupo.
 *
 * <p>Política de errores: el payload se valida antes de procesar. Un evento
 * defectuoso lanza {@link InvalidEventException}, el {@code DefaultErrorHandler}
 * lo reintenta (3 veces con backoff) y, si persiste, lo publica en
 * {@code workorder.audit.DLT} — sin interrumpir el consumo del grupo.</p>
 */
@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(topics = KafkaConfig.AUDIT_TOPIC, groupId = KafkaConfig.AUDIT_GROUP_ID)
    public void onEvent(WorkOrderEvent event,
                        @Header(name = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
                        @Header(name = KafkaHeaders.OFFSET, required = false) Long offset) {
        validate(event);
        log.info("Evento de auditoria recibido de Kafka: {} (orden {}, particion {}, offset {})",
                event.eventId(), event.workOrderId(), partition, offset);
        auditService.record(event);
    }

    private void validate(WorkOrderEvent event) {
        if (event == null) {
            throw new InvalidEventException("El evento es nulo");
        }
        if (event.eventId() == null || event.eventId().isBlank()) {
            throw new InvalidEventException("El evento no incluye eventId");
        }
        if (event.workOrderId() == null) {
            throw new InvalidEventException("El evento " + event.eventId() + " no incluye workOrderId");
        }
    }
}
