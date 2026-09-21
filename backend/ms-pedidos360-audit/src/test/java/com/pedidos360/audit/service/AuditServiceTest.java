package com.pedidos360.audit.service;

import com.pedidos360.audit.domain.AuditEvent;
import com.pedidos360.audit.domain.WorkOrderEvent;
import com.pedidos360.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository repository;

    @InjectMocks
    private AuditService service;

    @Test
    void recordMapsEventToAuditEntry() {
        WorkOrderEvent event = new WorkOrderEvent(
                "evt-1",
                "WORKORDER_CREATED",
                7L,
                "PENDING",
                "Cliente A",
                null,
                "luca.tapia@duocuc.cl",
                Instant.parse("2026-09-01T10:00:00Z"));

        service.record(event);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());

        AuditEvent saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("evt-1");
        assertThat(saved.getEventType()).isEqualTo("WORKORDER_CREATED");
        assertThat(saved.getWorkOrderId()).isEqualTo(7L);
        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getActor()).isEqualTo("luca.tapia@duocuc.cl");
        assertThat(saved.getEventTimestamp()).isEqualTo(Instant.parse("2026-09-01T10:00:00Z"));
    }

    @Test
    void timelineReturnsEventsOrderedByTimestamp() {
        AuditEvent event = new AuditEvent();
        event.setEventId("evt-1");

        when(repository.findByWorkOrderIdOrderByEventTimestampAsc(7L)).thenReturn(List.of(event));

        assertThat(service.timeline(7L)).containsExactly(event);
        verify(repository).findByWorkOrderIdOrderByEventTimestampAsc(7L);
    }

    @Test
    void findAllReturnsAllAuditEntries() {
        when(repository.findAll()).thenReturn(List.of(new AuditEvent(), new AuditEvent()));

        assertThat(service.findAll()).hasSize(2);
    }
}