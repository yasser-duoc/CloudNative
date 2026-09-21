package com.pedidos360.report.repository;

import com.pedidos360.report.domain.KpiMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiMetricRepository extends JpaRepository<KpiMetric, Long> {
}
