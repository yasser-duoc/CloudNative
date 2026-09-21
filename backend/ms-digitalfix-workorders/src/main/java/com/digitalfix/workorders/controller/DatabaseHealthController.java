package com.digitalfix.workorders.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de diagnóstico de la conexión a Oracle (requiere JWT válido).
 * GET /api/health/database
 */
@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    private final DataSource dataSource;

    public DatabaseHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> body = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(5);
            body.put("status", valid ? "UP" : "DOWN");
            body.put("url", conn.getMetaData().getURL());
            body.put("product", conn.getMetaData().getDatabaseProductName()
                    + " " + conn.getMetaData().getDatabaseProductVersion());
            return ResponseEntity.ok(body);
        } catch (SQLException e) {
            body.put("status", "DOWN");
            body.put("errorCode", e.getErrorCode());
            body.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }
}
