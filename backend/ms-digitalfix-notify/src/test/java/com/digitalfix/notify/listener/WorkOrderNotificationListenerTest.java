package com.digitalfix.notify.listener;

import com.digitalfix.notify.domain.WorkOrderEvent;
import com.digitalfix.notify.exception.RecoverableMessagingException;
import com.digitalfix.notify.service.NotificationService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Prueba la política de confirmación manual: ACK en éxito, NACK a DLQ en
 * mensaje inválido y reintento (relanzamiento) en error transitorio.
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Channel channel;

    @InjectMocks
    private WorkOrderNotificationListener listener;

    private WorkOrderEvent validEvent() {
        return new WorkOrderEvent(
                "evt-1", "WORK_ORDER_CREATED", 10L, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());
    }

    @Test
    void acksMessageWhenNotificationSucceeds() throws IOException {
        listener.onWorkOrderCreated(validEvent(), channel, 5L);

        verify(notificationService).sendEmail(any());
        verify(notificationService).sendPush(any());
        verify(channel).basicAck(5L, false);
        verify(channel, never()).basicNack(5L, false, false);
    }

    @Test
    void completedQueueSendsEmailOnlyAndAcks() throws IOException {
        listener.onWorkOrderCompleted(validEvent(), channel, 7L);

        verify(notificationService).sendEmail(any());
        verify(notificationService, never()).sendPush(any());
        verify(channel).basicAck(7L, false);
    }

    @Test
    void nacksToDlqWhenMessageIsInvalid() throws IOException {
        WorkOrderEvent invalid = new WorkOrderEvent(
                null, "WORK_ORDER_CREATED", 10L, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());

        listener.onWorkOrderCreated(invalid, channel, 9L);

        verify(channel).basicNack(9L, false, false);
        verify(channel, never()).basicAck(9L, false);
        verify(notificationService, never()).sendEmail(any());
    }

    @Test
    void rethrowsRecoverableErrorSoContainerRetriesBeforeDlq() throws IOException {
        doThrow(new RecoverableMessagingException("email no disponible"))
                .when(notificationService).sendEmail(any());

        assertThatThrownBy(() -> listener.onWorkOrderCreated(validEvent(), channel, 11L))
                .isInstanceOf(RecoverableMessagingException.class);

        verify(channel, never()).basicAck(11L, false);
        verify(channel, never()).basicNack(11L, false, false);
    }
}
