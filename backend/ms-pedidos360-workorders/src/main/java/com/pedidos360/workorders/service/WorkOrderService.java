package com.pedidos360.workorders.service;

import com.pedidos360.workorders.domain.WorkOrder;
import com.pedidos360.workorders.domain.WorkOrderStatus;
import com.pedidos360.workorders.domain.dto.WorkOrderRequest;
import com.pedidos360.workorders.domain.dto.WorkOrderResponse;
import com.pedidos360.workorders.event.WorkOrderEvent;
import com.pedidos360.workorders.publisher.WorkOrderEventPublisher;
import com.pedidos360.workorders.repository.WorkOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository repository;
    private final WorkOrderEventPublisher publisher;

    public WorkOrderService(WorkOrderRepository repository, WorkOrderEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public WorkOrderResponse create(WorkOrderRequest request, String createdBy) {
        WorkOrder order = new WorkOrder();
        order.setCustomerName(request.getCustomerName());
        order.setServiceId(request.getServiceId());
        order.setDescription(request.getDescription());
        order.setStatus(WorkOrderStatus.PENDING);
        order.setCreatedBy(createdBy);

        WorkOrder saved = repository.save(order);

        publisher.publish(new WorkOrderEvent(
                UUID.randomUUID().toString(),
                WorkOrderEvent.TYPE_CREATED,
                saved.getId(),
                saved.getStatus(),
                saved.getCustomerName(),
                null,
                createdBy,
                Instant.now()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public WorkOrderResponse updateStatus(Long id, WorkOrderStatus status, String actor) {
        WorkOrder order = findEntity(id);
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());

        WorkOrder saved = repository.save(order);

        String eventType = status == WorkOrderStatus.COMPLETED
                ? WorkOrderEvent.TYPE_COMPLETED
                : WorkOrderEvent.TYPE_STATUS_CHANGED;

        publisher.publish(new WorkOrderEvent(
                UUID.randomUUID().toString(),
                eventType,
                saved.getId(),
                saved.getStatus(),
                saved.getCustomerName(),
                null,
                actor,
                Instant.now()));

        return toResponse(saved);
    }

    private WorkOrder findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Work order not found: " + id));
    }

    private WorkOrderResponse toResponse(WorkOrder order) {
        WorkOrderResponse response = new WorkOrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setServiceId(order.getServiceId());
        response.setDescription(order.getDescription());
        response.setStatus(order.getStatus());
        response.setCreatedBy(order.getCreatedBy());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}
