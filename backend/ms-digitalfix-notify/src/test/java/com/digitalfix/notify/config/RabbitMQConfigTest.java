package com.digitalfix.notify.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la topología declarada en RabbitMQ: colas, exchanges, DLQs,
 * argumentos de dead-letter y bindings con sus routing keys.
 */
class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    private void assertDeadLettered(Queue queue, String expectedDlq) {
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", RabbitMQConfig.DLX)
                .containsEntry("x-dead-letter-routing-key", expectedDlq);
    }

    @Test
    void declaresDurableTopicExchangeAndDlx() {
        TopicExchange exchange = config.workOrderExchange();
        TopicExchange dlx = config.workOrderDlx();

        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.EXCHANGE);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(dlx.getName()).isEqualTo(RabbitMQConfig.DLX);
    }

    @Test
    void declaresNotificationQueuesWithTheirDlqs() {
        assertDeadLettered(config.createdQueue(), RabbitMQConfig.CREATED_DLQ);
        assertDeadLettered(config.statusQueue(), RabbitMQConfig.STATUS_DLQ);
        assertDeadLettered(config.completedQueue(), RabbitMQConfig.COMPLETED_DLQ);

        assertThat(config.createdQueue().getName()).isEqualTo("workorder.created.queue");
        assertThat(config.statusQueue().getName()).isEqualTo("workorder.status.queue");
        assertThat(config.completedQueue().getName()).isEqualTo("workorder.completed.queue");
    }

    @Test
    void declaresTicketQueueWithDlq() {
        Queue ticketQueue = config.ticketQueue();

        assertThat(ticketQueue.getName()).isEqualTo("workorder.ticket.queue");
        assertDeadLettered(ticketQueue, RabbitMQConfig.TICKET_DLQ);
        assertThat(config.ticketDlq().getName()).isEqualTo("workorder.ticket.queue.dlq");
    }

    @Test
    void declaresDlqQueuesWithoutDeadLettering() {
        assertThat(config.createdDlq().getArguments()).doesNotContainKey("x-dead-letter-exchange");
        assertThat(config.statusDlq().getArguments()).doesNotContainKey("x-dead-letter-exchange");
        assertThat(config.completedDlq().getArguments()).doesNotContainKey("x-dead-letter-exchange");
        assertThat(config.ticketDlq().getArguments()).doesNotContainKey("x-dead-letter-exchange");
    }

    @Test
    void bindsMainQueuesToTopicExchangeWithRoutingKeys() {
        TopicExchange exchange = config.workOrderExchange();

        Binding created = config.createdBinding(config.createdQueue(), exchange);
        Binding status = config.statusBinding(config.statusQueue(), exchange);
        Binding completed = config.completedBinding(config.completedQueue(), exchange);
        Binding ticket = config.ticketBinding(config.ticketQueue(), exchange);

        assertThat(created.getExchange()).isEqualTo(RabbitMQConfig.EXCHANGE);
        assertThat(created.getRoutingKey()).isEqualTo("workorder.created");
        assertThat(status.getRoutingKey()).isEqualTo("workorder.status.#");
        assertThat(completed.getRoutingKey()).isEqualTo("workorder.completed");

        assertThat(ticket.getExchange()).isEqualTo(RabbitMQConfig.EXCHANGE);
        assertThat(ticket.getRoutingKey()).isEqualTo("workorder.created");
        assertThat(ticket.getDestination()).isEqualTo("workorder.ticket.queue");
    }

    @Test
    void bindsDlqQueuesToDlxWithTheirOwnRoutingKeys() {
        TopicExchange dlx = config.workOrderDlx();

        Binding createdDlqBinding = config.createdDlqBinding(config.createdDlq(), dlx);
        Binding ticketDlqBinding = config.ticketDlqBinding(config.ticketDlq(), dlx);

        assertThat(createdDlqBinding.getExchange()).isEqualTo(RabbitMQConfig.DLX);
        assertThat(createdDlqBinding.getRoutingKey()).isEqualTo(RabbitMQConfig.CREATED_DLQ);
        assertThat(ticketDlqBinding.getExchange()).isEqualTo(RabbitMQConfig.DLX);
        assertThat(ticketDlqBinding.getRoutingKey()).isEqualTo(RabbitMQConfig.TICKET_DLQ);
    }
}
