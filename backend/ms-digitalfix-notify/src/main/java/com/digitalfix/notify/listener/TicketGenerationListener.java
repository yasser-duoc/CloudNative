package com.digitalfix.notify.listener;

import com.digitalfix.notify.config.RabbitMQConfig;
import com.digitalfix.notify.domain.WorkOrderEvent;
import com.digitalfix.notify.exception.InvalidMessageException;
import com.digitalfix.notify.exception.RecoverableMessagingException;
import com.digitalfix.notify.service.TicketGenerationService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de generación de tickets. Misma política de confirmación manual
 * que las notificaciones: ACK explícito en éxito, reintentos para errores
 * transitorios y NACK a DLQ para mensajes inválidos.
 */
@Component
public class TicketGenerationListener {

    private static final Logger log = LoggerFactory.getLogger(TicketGenerationListener.class);

    private final TicketGenerationService ticketGenerationService;

    public TicketGenerationListener(TicketGenerationService ticketGenerationService) {
        this.ticketGenerationService = ticketGenerationService;
    }

    @RabbitListener(queues = RabbitMQConfig.TICKET_QUEUE)
    public void onWorkOrderCreated(WorkOrderEvent event, Channel channel,
                                   @Header(name = AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            validate(event);
            log.info("Mensaje recibido en '{}': {}", RabbitMQConfig.TICKET_QUEUE, event.eventId());

            ticketGenerationService.generate(event);

            channel.basicAck(deliveryTag, false);
            log.debug("ACK del mensaje {} en {}", event.eventId(), RabbitMQConfig.TICKET_QUEUE);
        } catch (RecoverableMessagingException ex) {
            log.warn("Error transitorio generando ticket para {}: {} (será reintentado y luego derivado a DLQ)",
                    event == null ? null : event.eventId(), ex.getMessage());
            throw ex;
        } catch (InvalidMessageException ex) {
            log.error("Mensaje inválido en {} ({}): enviado a DLQ. Payload: {}",
                    RabbitMQConfig.TICKET_QUEUE, ex.getMessage(), event);
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
