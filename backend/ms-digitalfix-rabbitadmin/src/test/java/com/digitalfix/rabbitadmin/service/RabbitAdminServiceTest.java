package com.digitalfix.rabbitadmin.service;

import com.digitalfix.rabbitadmin.dto.BindingRequest;
import com.digitalfix.rabbitadmin.dto.ExchangeRequest;
import com.digitalfix.rabbitadmin.dto.QueueRequest;
import com.digitalfix.rabbitadmin.exception.ConflictException;
import com.digitalfix.rabbitadmin.exception.InvalidResourceException;
import com.digitalfix.rabbitadmin.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @InjectMocks
    private RabbitAdminService service;

    @Test
    void createsDurableQueueWhenItDoesNotExist() {
        when(rabbitAdmin.getQueueProperties("pedidos.nueva")).thenReturn(null);

        Queue queue = service.createQueue(new QueueRequest("pedidos.nueva", true, null, null));

        assertThat(queue.getName()).isEqualTo("pedidos.nueva");
        assertThat(queue.isDurable()).isTrue();
        verify(rabbitAdmin).declareQueue(any(Queue.class));
    }

    @Test
    void rejectsQueueCreationWhenAlreadyExists() {
        when(rabbitAdmin.getQueueProperties("workorder.created.queue")).thenReturn(new Properties());

        assertThatThrownBy(() -> service.createQueue(new QueueRequest("workorder.created.queue", true, null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsReservedQueueNames() {
        assertThatThrownBy(() -> service.createQueue(new QueueRequest("amq.default", true, null, null)))
                .isInstanceOf(InvalidResourceException.class);
    }

    @Test
    void createQueueWithDeadLetterArguments() {
        when(rabbitAdmin.getQueueProperties("pedidos.reintentos")).thenReturn(null);

        Queue queue = service.createQueue(
                new QueueRequest("pedidos.reintentos", true, "pedidos.dlx", "pedidos.reintentos.dlq"));

        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", "pedidos.dlx")
                .containsEntry("x-dead-letter-routing-key", "pedidos.reintentos.dlq");
    }

    @Test
    void deletesExistingQueue() {
        when(rabbitAdmin.getQueueProperties("pedidos.nueva")).thenReturn(new Properties());

        service.deleteQueue("pedidos.nueva");

        verify(rabbitAdmin).deleteQueue("pedidos.nueva");
    }

    @Test
    void deleteQueueThrowsNotFoundWhenMissing() {
        when(rabbitAdmin.getQueueProperties(anyString())).thenReturn(null);

        assertThatThrownBy(() -> service.deleteQueue("inexistente"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createsTopicExchange() {
        ExchangeRequest request = new ExchangeRequest("pedidos.eventos", "topic", true);

        service.createExchange(request);

        verify(rabbitAdmin).declareExchange(any(org.springframework.amqp.core.Exchange.class));
    }

    @Test
    void createsQueueBindingWithRoutingKey() {
        BindingRequest request = new BindingRequest("pedidos.eventos", "pedidos.nueva", "queue", "pedido.creado");

        Binding binding = service.createBinding(request);

        assertThat(binding.getExchange()).isEqualTo("pedidos.eventos");
        assertThat(binding.getDestination()).isEqualTo("pedidos.nueva");
        assertThat(binding.getDestinationType()).isEqualTo(Binding.DestinationType.QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo("pedido.creado");
        verify(rabbitAdmin).declareBinding(any(Binding.class));
    }

    @Test
    void rejectsExchangeToExchangeBindingWithRoutingKey() {
        BindingRequest request = new BindingRequest("pedidos.eventos", "otro.exchange", "exchange", "clave");

        assertThatThrownBy(() -> service.createBinding(request))
                .isInstanceOf(InvalidResourceException.class);
    }
}
