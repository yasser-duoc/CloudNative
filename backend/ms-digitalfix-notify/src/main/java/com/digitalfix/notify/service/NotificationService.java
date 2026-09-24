package com.digitalfix.notify.service;

import com.digitalfix.notify.domain.WorkOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendEmail(WorkOrderEvent event) {
        log.info("[EMAIL] Enviando correo de evento '{}' al cliente '{}' (orden {})",
                event.eventType(), event.customerEmail(), event.workOrderId());
    }

    public void sendPush(WorkOrderEvent event) {
        log.info("[PUSH] Enviando notificación push de evento '{}' (orden {})",
                event.eventType(), event.workOrderId());
    }
}
