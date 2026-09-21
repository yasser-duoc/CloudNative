package com.pedidos360.workorders.publisher;

import com.pedidos360.workorders.config.KafkaConfig;
import com.pedidos360.workorders.config.RabbitMQConfig;
import com.pedidos360.workorders.event.WorkOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, WorkOrderEvent> kafkaTemplate;

    public WorkOrderEventPublisher(RabbitTemplate rabbitTemplate,
                                   KafkaTemplate<String, WorkOrderEvent> kafkaTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(WorkOrderEvent event) {
        String routingKey = switch (event.eventType()) {
            case WorkOrderEvent.TYPE_CREATED -> RabbitMQConfig.CREATED_ROUTING_KEY;
            case WorkOrderEvent.TYPE_COMPLETED -> RabbitMQConfig.COMPLETED_ROUTING_KEY;
            default -> RabbitMQConfig.STATUS_ROUTING_KEY;
        };

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
            kafkaTemplate.send(KafkaConfig.AUDIT_TOPIC, event.eventId(), event);
            kafkaTemplate.send(KafkaConfig.KPI_TOPIC, event.eventId(), event);
        } catch (Exception ex) {
            log.error("Error publicando evento {} en RabbitMQ/Kafka: {}",
                    event.eventId(), ex.getMessage(), ex);
        }
    }
}
