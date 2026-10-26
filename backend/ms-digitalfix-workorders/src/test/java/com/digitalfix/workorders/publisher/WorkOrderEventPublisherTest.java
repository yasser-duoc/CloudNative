package com.digitalfix.workorders.publisher;

import com.digitalfix.workorders.config.RabbitMQConfig;
import com.digitalfix.workorders.event.WorkOrderEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * La mensajería es best-effort: un fallo del broker nunca debe propagarse
 * y afectar la lógica de negocio de las órdenes.
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WorkOrderEventPublisher publisher;

    private WorkOrderEvent event(String eventType) {
        return new WorkOrderEvent(
                "evt-1", eventType, 10L, "CREADA",
                "Cliente Test", null, "tester@digitalfix.cl", Instant.now());
    }

    @Test
    void publishesCreatedEventToCreatedRoutingKey() {
        WorkOrderEvent event = event(WorkOrderEvent.TYPE_CREATED);

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("workorder.created"), eq(event));
    }

    @Test
    void publishesStatusChangedEventToStatusRoutingKey() {
        WorkOrderEvent event = event(WorkOrderEvent.TYPE_STATUS_CHANGED);

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("workorder.status"), eq(event));
    }

    @Test
    void publishesCompletedEventToCompletedRoutingKey() {
        WorkOrderEvent event = event(WorkOrderEvent.TYPE_COMPLETED);

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq("workorder.completed"), eq(event));
    }

    @Test
    void brokerFailureDoesNotAffectBusinessLogic() {
        doThrow(new AmqpException("connection refused"))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        assertThatCode(() -> publisher.publish(event(WorkOrderEvent.TYPE_CREATED)))
                .doesNotThrowAnyException();
    }
}
