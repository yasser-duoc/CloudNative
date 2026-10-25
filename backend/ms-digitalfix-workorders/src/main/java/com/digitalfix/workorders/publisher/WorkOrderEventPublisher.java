package com.digitalfix.workorders.publisher;

import com.digitalfix.workorders.config.RabbitMQConfig;
import com.digitalfix.workorders.event.WorkOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public WorkOrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(WorkOrderEvent event) {
        String routingKey = switch (event.eventType()) {
            case WorkOrderEvent.TYPE_CREATED -> RabbitMQConfig.CREATED_ROUTING_KEY;
            case WorkOrderEvent.TYPE_COMPLETED -> RabbitMQConfig.COMPLETED_ROUTING_KEY;
            default -> RabbitMQConfig.STATUS_ROUTING_KEY;
        };

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
        } catch (Exception ex) {
            log.error("Error publicando evento {} en RabbitMQ: {}", event.eventId(), ex.getMessage(), ex);
        }
    }
}
