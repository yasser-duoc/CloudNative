package com.digitalfix.rabbitadmin.service;

import com.digitalfix.rabbitadmin.dto.BindingRequest;
import com.digitalfix.rabbitadmin.dto.ExchangeRequest;
import com.digitalfix.rabbitadmin.dto.QueueInfoResponse;
import com.digitalfix.rabbitadmin.dto.QueueRequest;
import com.digitalfix.rabbitadmin.exception.ConflictException;
import com.digitalfix.rabbitadmin.exception.InvalidResourceException;
import com.digitalfix.rabbitadmin.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Encapsula toda la administración de RabbitMQ (creación y eliminación de
 * colas, exchanges y bindings). Los controladores solo invocan esta API de
 * alto nivel y no conocen detalles internos del broker.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // ---------- Colas ----------

    public Queue createQueue(QueueRequest request) {
        validateName(request.name(), "cola");

        if (queueExists(request.name())) {
            throw new ConflictException("La cola '" + request.name() + "' ya existe");
        }

        QueueBuilder builder = QueueBuilder.durable(request.name());
        if (Boolean.FALSE.equals(request.durable())) {
            builder = QueueBuilder.nonDurable(request.name());
        }
        if (request.deadLetterExchange() != null && !request.deadLetterExchange().isBlank()) {
            builder = builder.deadLetterExchange(request.deadLetterExchange());
            if (request.deadLetterRoutingKey() != null && !request.deadLetterRoutingKey().isBlank()) {
                builder = builder.deadLetterRoutingKey(request.deadLetterRoutingKey());
            }
        }

        Queue queue = builder.build();
        rabbitAdmin.declareQueue(queue);
        log.info("Cola '{}' creada (durable={}, DLX={})",
                request.name(), !Boolean.FALSE.equals(request.durable()), request.deadLetterExchange());
        return queue;
    }

    public QueueInfoResponse getQueueInfo(String name) {
        validateName(name, "cola");
        Properties props = rabbitAdmin.getQueueProperties(name);
        if (props == null) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }
        return new QueueInfoResponse(
                String.valueOf(props.get(RabbitAdmin.QUEUE_NAME)),
                toInt(props.get(RabbitAdmin.QUEUE_MESSAGE_COUNT)),
                toInt(props.get(RabbitAdmin.QUEUE_CONSUMER_COUNT)));
    }

    public void deleteQueue(String name) {
        validateName(name, "cola");

        if (!queueExists(name)) {
            throw new ResourceNotFoundException("La cola '" + name + "' no existe");
        }

        rabbitAdmin.deleteQueue(name);
        log.info("Cola '{}' eliminada", name);
    }

    public boolean queueExists(String name) {
        return rabbitAdmin.getQueueProperties(name) != null;
    }    // ---------- Exchanges ----------

    public Exchange createExchange(ExchangeRequest request) {
        validateName(request.name(), "exchange");

        boolean durable = !Boolean.FALSE.equals(request.durable());
        Exchange exchange = switch (request.type()) {
            case "direct" -> durable
                    ? ExchangeBuilder.directExchange(request.name()).durable(true).build()
                    : ExchangeBuilder.directExchange(request.name()).durable(false).autoDelete().build();
            case "topic" -> durable
                    ? ExchangeBuilder.topicExchange(request.name()).durable(true).build()
                    : ExchangeBuilder.topicExchange(request.name()).durable(false).autoDelete().build();
            case "fanout" -> durable
                    ? ExchangeBuilder.fanoutExchange(request.name()).durable(true).build()
                    : ExchangeBuilder.fanoutExchange(request.name()).durable(false).autoDelete().build();
            case "headers" -> durable
                    ? ExchangeBuilder.headersExchange(request.name()).durable(true).build()
                    : ExchangeBuilder.headersExchange(request.name()).durable(false).autoDelete().build();
            default -> throw new InvalidResourceException(
                    "Tipo de exchange no soportado: " + request.type());
        };

        rabbitAdmin.declareExchange(exchange);
        log.info("Exchange '{}' de tipo {} creado", request.name(), request.type());
        return exchange;
    }

    public void deleteExchange(String name) {
        validateName(name, "exchange");

        boolean deleted = rabbitAdmin.deleteExchange(name);
        if (!deleted) {
            throw new ResourceNotFoundException("El exchange '" + name + "' no existe");
        }
        log.info("Exchange '{}' eliminado", name);
    }

    // ---------- Bindings ----------

    public Binding createBinding(BindingRequest request) {
        Binding binding = toBinding(request);
        rabbitAdmin.declareBinding(binding);
        log.info("Binding creado: {} '{}' -> exchange '{}' con routing key '{}'",
                request.destinationType(), request.destination(),
                request.exchange(), request.routingKey());
        return binding;
    }

    public void deleteBinding(String exchange, String destination, String destinationType, String routingKey) {
        BindingRequest request = new BindingRequest(exchange, destination, destinationType,
                routingKey == null ? "" : routingKey);
        Binding binding = toBinding(request);
        rabbitAdmin.removeBinding(binding);
        log.info("Binding eliminado: {} '{}' -> exchange '{}' con routing key '{}'",
                destinationType, destination, exchange, routingKey);
    }

    private Binding toBinding(BindingRequest request) {
        if (request.destinationType() == null) {
            throw new InvalidResourceException("El tipo de destino es obligatorio");
        }
        Binding.DestinationType type = switch (request.destinationType()) {
            case "queue" -> Binding.DestinationType.QUEUE;
            case "exchange" -> Binding.DestinationType.EXCHANGE;
            default -> throw new InvalidResourceException(
                    "Tipo de destino no soportado: " + request.destinationType());
        };

        String routingKey = request.routingKey() == null ? "" : request.routingKey();
        if (type == Binding.DestinationType.EXCHANGE && !routingKey.isBlank()) {
            throw new InvalidResourceException(
                    "Un binding exchange-to-exchange solo admite routing key vacía");
        }

        return new Binding(request.destination(), type, request.exchange(), routingKey, null);
    }

    private void validateName(String name, String resourceType) {
        if (name == null || name.isBlank()) {
            throw new InvalidResourceException("El nombre de la " + resourceType + " es obligatorio");
        }
        if (name.startsWith("amq.") || name.equals("default")) {
            throw new InvalidResourceException(
                    "El nombre '" + name + "' está reservado por RabbitMQ y no puede administrarse");
        }
    }

    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
