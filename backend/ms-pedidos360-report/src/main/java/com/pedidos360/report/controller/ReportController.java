package com.pedidos360.report.controller;

import com.pedidos360.report.domain.KpiMetric;
import com.pedidos360.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/kpis")
    public Map<String, Long> summary() {
        return reportService.summary();
    }

    @GetMapping("/kpis/raw")
    public List<KpiMetric> raw() {
        return reportService.findAll();
    }
}
