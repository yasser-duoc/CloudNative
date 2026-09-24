package com.digitalfix.report.service;

import com.digitalfix.report.domain.KpiMetric;
import com.digitalfix.report.domain.WorkOrderEvent;
import com.digitalfix.report.repository.KpiMetricRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private KpiMetricRepository repository;

    @InjectMocks
    private ReportService service;

    @Test
    void recordPersistsMetricForEvent() {
        WorkOrderEvent event = new WorkOrderEvent(
                "evt-1",
                "WORKORDER_COMPLETED",
                7L,
                "COMPLETED",
                "Cliente A",
                null,
                "luca.tapia@duocuc.cl",
                Instant.parse("2026-09-01T10:00:00Z"));

        service.record(event);

        ArgumentCaptor<KpiMetric> captor = ArgumentCaptor.forClass(KpiMetric.class);
        verify(repository).save(captor.capture());

        KpiMetric metric = captor.getValue();
        assertThat(metric.getMetricName()).isEqualTo("WORKORDER_COMPLETED");
        assertThat(metric.getMetricValue()).isEqualTo(1.0);
        assertThat(metric.getWorkOrderId()).isEqualTo(7L);
        assertThat(metric.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void summaryGroupsMetricsByMetricName() {
        KpiMetric created = new KpiMetric();
        created.setMetricName("WORKORDER_CREATED");
        KpiMetric completed = new KpiMetric();
        completed.setMetricName("WORKORDER_COMPLETED");

        when(repository.findAll()).thenReturn(List.of(created, created, completed));

        Map<String, Long> summary = service.summary();

        assertThat(summary)
                .containsEntry("WORKORDER_CREATED", 2L)
                .containsEntry("WORKORDER_COMPLETED", 1L);
    }

    @Test
    void findAllReturnsAllMetrics() {
        when(repository.findAll()).thenReturn(List.of(new KpiMetric(), new KpiMetric()));

        assertThat(service.findAll()).hasSize(2);
    }
}