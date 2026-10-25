package com.digitalfix.notify.listener;

import com.digitalfix.notify.config.RabbitMQConfig;
import com.digitalfix.notify.domain.WorkOrderEvent;
import com.digitalfix.notify.service.TicketGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TicketGenerationListener {

    private static final Logger log = LoggerFactory.getLogger(TicketGenerationListener.class);

    private final TicketGenerationService ticketGenerationService;

    public TicketGenerationListener(TicketGenerationService ticketGenerationService) {
        this.ticketGenerationService = ticketGenerationService;
    }

    @RabbitListener(queues = RabbitMQConfig.TICKET_QUEUE)
    public void onWorkOrderCreated(WorkOrderEvent event) {
        log.info("Mensaje recibido en '{}': {}", RabbitMQConfig.TICKET_QUEUE, event.eventId());
        ticketGenerationService.generate(event);
    }
}
