package com.digitalfix.rabbitadmin.controller;

import com.digitalfix.rabbitadmin.dto.BindingRequest;
import com.digitalfix.rabbitadmin.dto.ExchangeRequest;
import com.digitalfix.rabbitadmin.dto.QueueInfoResponse;
import com.digitalfix.rabbitadmin.dto.QueueRequest;
import com.digitalfix.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST del administrador de RabbitMQ. Rutas, verbos y recursos alineados
 * con prácticas REST: /api/rabbit/queues, /api/rabbit/exchanges y /api/rabbit/bindings.
 */
@RestController
@RequestMapping("/api/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    public RabbitAdminController(RabbitAdminService rabbitAdminService) {
        this.rabbitAdminService = rabbitAdminService;
    }

    // ---------- Colas ----------

    @PostMapping("/queues")
    public ResponseEntity<Queue> createQueue(@Valid @RequestBody QueueRequest request) {
        Queue queue = rabbitAdminService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(queue);
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<QueueInfoResponse> getQueue(@PathVariable String name) {
        return ResponseEntity.ok(rabbitAdminService.getQueueInfo(name));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        rabbitAdminService.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    // ---------- Exchanges ----------

    @PostMapping("/exchanges")
    public ResponseEntity<Exchange> createExchange(@Valid @RequestBody ExchangeRequest request) {
        Exchange exchange = rabbitAdminService.createExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(exchange);
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        rabbitAdminService.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    // ---------- Bindings ----------

    @PostMapping("/bindings")
    public ResponseEntity<Binding> createBinding(@Valid @RequestBody BindingRequest request) {
        Binding binding = rabbitAdminService.createBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(binding);
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@RequestParam String exchange,
                                              @RequestParam String destination,
                                              @RequestParam String destinationType,
                                              @RequestParam(required = false) String routingKey) {
        rabbitAdminService.deleteBinding(exchange, destination, destinationType, routingKey);
        return ResponseEntity.noContent().build();
    }
}
