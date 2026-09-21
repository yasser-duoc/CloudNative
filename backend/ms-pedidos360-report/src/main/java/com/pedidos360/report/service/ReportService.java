package com.pedidos360.report.service;

import com.pedidos360.report.domain.KpiMetric;
import com.pedidos360.report.domain.WorkOrderEvent;
import com.pedidos360.report.repository.KpiMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService {

    private final KpiMetricRepository repository;

    public ReportService(KpiMetricRepository repository) {
        this.repository = repository;
    }

    public void record(WorkOrderEvent event) {
        KpiMetric metric = new KpiMetric();
        metric.setMetricName(event.eventType());
        metric.setMetricValue(1.0);
        metric.setWorkOrderId(event.workOrderId());
        metric.setStatus(event.status());
        repository.save(metric);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> summary() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(KpiMetric::getMetricName, Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public List<KpiMetric> findAll() {
        return repository.findAll();
    }
}
