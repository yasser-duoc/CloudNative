package com.pedidos360.notify.listener;

import com.pedidos360.notify.config.RabbitMQConfig;
import com.pedidos360.notify.domain.WorkOrderEvent;
import com.pedidos360.notify.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderNotificationListener.class);

    private final NotificationService notificationService;

    public WorkOrderNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.CREATED_QUEUE)
    public void onWorkOrderCreated(WorkOrderEvent event) {
        log.info("Mensaje recibido en '{}': {}", RabbitMQConfig.CREATED_QUEUE, event.eventId());
        notificationService.sendEmail(event);
        notificationService.sendPush(event);
    }

    @RabbitListener(queues = RabbitMQConfig.STATUS_QUEUE)
    public void onWorkOrderStatusChanged(WorkOrderEvent event) {
        log.info("Mensaje recibido en '{}': {}", RabbitMQConfig.STATUS_QUEUE, event.eventId());
        notificationService.sendEmail(event);
        notificationService.sendPush(event);
    }

    @RabbitListener(queues = RabbitMQConfig.COMPLETED_QUEUE)
    public void onWorkOrderCompleted(WorkOrderEvent event) {
        log.info("Mensaje recibido en '{}': {}", RabbitMQConfig.COMPLETED_QUEUE, event.eventId());
        notificationService.sendEmail(event);
    }
}
