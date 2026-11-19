package com.digitalfix.report.listener;

import com.digitalfix.report.config.KafkaConfig;
import com.digitalfix.report.domain.WorkOrderEvent;
import com.digitalfix.report.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumidor del topic {@code workorder.kpi}: alimenta los KPIs de órdenes de
 * trabajo. Al ser un log, el grupo puede reprocesar el historial (replay).
 *
 * <p>Política de errores: payload validado antes de procesar; un evento
 * defectuoso se reintenta y termina en {@code workorder.kpi.DLT}.</p>
 */
@Component
public class KpiListener {

    private static final Logger log = LoggerFactory.getLogger(KpiListener.class);

    private final ReportService reportService;

    public KpiListener(ReportService reportService) {
        this.reportService = reportService;
    }

    @KafkaListener(topics = KafkaConfig.KPI_TOPIC, groupId = KafkaConfig.KPI_GROUP_ID)
    public void onEvent(WorkOrderEvent event,
                        @Header(name = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
                        @Header(name = KafkaHeaders.OFFSET, required = false) Long offset) {
        validate(event);
        log.info("Evento KPI recibido de Kafka: {} (orden {}, particion {}, offset {})",
                event.eventId(), event.workOrderId(), partition, offset);
        reportService.record(event);
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
