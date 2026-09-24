package com.digitalfix.report.repository;

import com.digitalfix.report.domain.KpiMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiMetricRepository extends JpaRepository<KpiMetric, Long> {
}
