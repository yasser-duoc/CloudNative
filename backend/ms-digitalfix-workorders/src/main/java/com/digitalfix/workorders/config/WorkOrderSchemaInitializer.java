package com.digitalfix.workorders.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class WorkOrderSchemaInitializer {

    @Bean
    CommandLineRunner migrateWorkOrderStatusConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("""
                    ALTER TABLE work_orders
                    DROP CONSTRAINT IF EXISTS work_orders_status_check
                    """);
            jdbcTemplate.execute("""
                    UPDATE work_orders
                    SET status = CASE status
                        WHEN 'PENDING' THEN 'CREADA'
                        WHEN 'ASSIGNED' THEN 'ASIGNADA'
                        WHEN 'IN_PROGRESS' THEN 'EN_EJECUCIÓN'
                        WHEN 'ON_HOLD' THEN 'EN_DESPLAZAMIENTO'
                        WHEN 'COMPLETED' THEN 'CERRADA'
                        WHEN 'CANCELLED' THEN 'CANCELADA'
                        ELSE status
                    END
                    """);
            jdbcTemplate.execute("""
                    ALTER TABLE work_orders
                    ADD CONSTRAINT work_orders_status_check
                    CHECK (status IN (
                        'CREADA',
                        'ASIGNADA',
                        'EN_DESPLAZAMIENTO',
                        'EN_EJECUCIÓN',
                        'CERRADA',
                        'CANCELADA'
                    ))
                    """);
        };
    }
}
