package com.digitalfix.notify.listener;

import com.digitalfix.notify.domain.WorkOrderEvent;
import com.digitalfix.notify.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WorkOrderNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WorkOrderNotificationListener listener;

    private WorkOrderEvent event() {
        return new WorkOrderEvent(
                "evt-1", "WORK_ORDER_CREATED", 10L, "CREADA",
                "Cliente Test", "cliente@test.cl", "tester@digitalfix.cl", Instant.now());
    }

    @Test
    void onCreatedSendsEmailAndPush() {
        listener.onWorkOrderCreated(event());

        verify(notificationService).sendEmail(org.mockito.ArgumentMatchers.any());
        verify(notificationService).sendPush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onStatusChangedSendsEmailAndPush() {
        listener.onWorkOrderStatusChanged(event());

        verify(notificationService).sendEmail(org.mockito.ArgumentMatchers.any());
        verify(notificationService).sendPush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onCompletedSendsEmailOnly() {
        listener.onWorkOrderCompleted(event());

        verify(notificationService).sendEmail(org.mockito.ArgumentMatchers.any());
        verifyNoMoreInteractions(notificationService);
    }
}
