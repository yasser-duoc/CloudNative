package com.digitalfix.notify.listener;

import com.digitalfix.notify.config.RabbitMQConfig;
import com.digitalfix.notify.domain.WorkOrderEvent;
import com.digitalfix.notify.exception.InvalidMessageException;
import com.digitalfix.notify.exception.RecoverableMessagingException;
import com.digitalfix.notify.service.NotificationService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de notificaciones. Política de confirmación manual:
 *
 * <ul>
 *   <li>Éxito: {@code basicAck} explícito.</li>
 *   <li>Error transitorio ({@link RecoverableMessagingException}): se relanza para que
 *       el contenedor reintente (3 intentos con backoff). Si persiste, el contenedor
 *       hace NACK sin requeue y el mensaje va a la DLQ.</li>
 *   <li>Mensaje inválido o error de negocio: {@code basicNack} sin requeue,
 *       enviando el mensaje directamente a la DLQ.</li>
 * </ul>
 */
@Component
public class WorkOrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderNotificationListener.class);

    private final NotificationService notificationService;

    public WorkOrderNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.CREATED_QUEUE)
    public void onWorkOrderCreated(WorkOrderEvent event, Channel channel,
                                   @Header(name = AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        consume(RabbitMQConfig.CREATED_QUEUE, event, channel, deliveryTag, true);
    }

    @RabbitListener(queues = RabbitMQConfig.STATUS_QUEUE)
    public void onWorkOrderStatusChanged(WorkOrderEvent event, Channel channel,
                                         @Header(name = AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        consume(RabbitMQConfig.STATUS_QUEUE, event, channel, deliveryTag, true);
    }

    @RabbitListener(queues = RabbitMQConfig.COMPLETED_QUEUE)
    public void onWorkOrderCompleted(WorkOrderEvent event, Channel channel,
                                     @Header(name = AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        consume(RabbitMQConfig.COMPLETED_QUEUE, event, channel, deliveryTag, false);
    }

    private void consume(String queue, WorkOrderEvent event, Channel channel,
                         long deliveryTag, boolean includePush) throws IOException {
        try {
            validate(event);
            log.info("Mensaje recibido en '{}': {}", queue, event.eventId());

            notificationService.sendEmail(event);
            if (includePush) {
                notificationService.sendPush(event);
            }

            channel.basicAck(deliveryTag, false);
            log.debug("ACK del mensaje {} en {}", event.eventId(), queue);
        } catch (RecoverableMessagingException ex) {
            log.warn("Error transitorio procesando {} en {}: {} (será reintentado y luego derivado a DLQ)",
                    event.eventId(), queue, ex.getMessage());
            throw ex;
        } catch (InvalidMessageException ex) {
            log.error("Mensaje inválido en {} ({}): enviado a DLQ. Payload: {}",
                    queue, ex.getMessage(), event);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void validate(WorkOrderEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()) {
            throw new InvalidMessageException("El evento no incluye eventId");
        }
        if (event.workOrderId() == null) {
            throw new InvalidMessageException("El evento no incluye workOrderId");
        }
    }
}
