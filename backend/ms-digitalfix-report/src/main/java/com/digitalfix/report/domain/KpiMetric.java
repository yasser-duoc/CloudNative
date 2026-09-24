package com.digitalfix.report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "KPI_METRICS")
public class KpiMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kpi_metric_seq")
    @SequenceGenerator(name = "kpi_metric_seq", sequenceName = "KPI_METRIC_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "METRIC_NAME", nullable = false, length = 80)
    private String metricName;

    @Column(name = "METRIC_VALUE", nullable = false)
    private Double metricValue;

    @Column(name = "WORK_ORDER_ID")
    private Long workOrderId;

    @Column(name = "STATUS", length = 30)
    private String status;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Double metricValue) {
        this.metricValue = metricValue;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
