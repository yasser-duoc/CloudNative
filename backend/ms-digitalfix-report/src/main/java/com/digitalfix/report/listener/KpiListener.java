package com.digitalfix.report.listener;

import com.digitalfix.report.config.KafkaConfig;
import com.digitalfix.report.domain.WorkOrderEvent;
import com.digitalfix.report.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KpiListener {

    private static final Logger log = LoggerFactory.getLogger(KpiListener.class);

    private final ReportService reportService;

    public KpiListener(ReportService reportService) {
        this.reportService = reportService;
    }

    @KafkaListener(topics = KafkaConfig.KPI_TOPIC, groupId = KafkaConfig.KPI_GROUP_ID)
    public void onEvent(WorkOrderEvent event) {
        log.info("Evento KPI recibido: {} (orden {})", event.eventId(), event.workOrderId());
        reportService.record(event);
    }
}
