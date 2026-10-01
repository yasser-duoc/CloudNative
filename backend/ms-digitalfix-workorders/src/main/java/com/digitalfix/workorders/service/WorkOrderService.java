package com.digitalfix.workorders.service;

import com.digitalfix.workorders.audit.AuditEventClient;
import com.digitalfix.workorders.audit.AuditEventFactory;
import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.domain.dto.WorkOrderRequest;
import com.digitalfix.workorders.domain.dto.WorkOrderResponse;
import com.digitalfix.workorders.repository.WorkOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Instant;

@Service
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository repository;
    private final AuditEventClient auditEventClient;

    public WorkOrderService(WorkOrderRepository repository, AuditEventClient auditEventClient) {
        this.repository = repository;
        this.auditEventClient = auditEventClient;
    }

    public WorkOrderResponse create(WorkOrderRequest request, String createdBy) {
        return create(request, createdBy, null);
    }

    public WorkOrderResponse create(WorkOrderRequest request, String createdBy, String bearerToken) {
        WorkOrder order = new WorkOrder();
        order.setCustomerName(request.getCustomerName());
        order.setServiceId(request.getServiceId());
        order.setDescription(request.getDescription());
        order.setStatus(WorkOrderStatus.CREADA);
        order.setCreatedBy(createdBy);
        order.setAssignedTo(request.getAssignedTo());

        WorkOrder saved = repository.save(order);
        recordAudit("WORK_ORDER_CREATED", saved, createdBy, bearerToken);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> findAll(WorkOrderStatus status, Instant from, Instant to) {
        List<WorkOrder> orders = status == null ? repository.findAll() : repository.findByStatus(status);
        return orders.stream()
                .filter(order -> from == null || !order.getCreatedAt().isBefore(from))
                .filter(order -> to == null || !order.getCreatedAt().isAfter(to))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public WorkOrderResponse updateStatus(Long id, WorkOrderStatus status, String actor) {
        return updateStatus(id, status, actor, null);
    }

    public WorkOrderResponse updateStatus(Long id, WorkOrderStatus status, String actor, String bearerToken) {
        WorkOrder order = findEntity(id);
        if (status == null || !isTransitionAllowed(order.getStatus(), status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid work order status transition");
        }
        if (status == WorkOrderStatus.EN_EJECUCIÓN
                && (order.getAssignedTo() == null || order.getAssignedTo().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A work order must be assigned before execution");
        }
        if (status == WorkOrderStatus.ASIGNADA && (order.getAssignedTo() == null || order.getAssignedTo().isBlank())) {
            order.setAssignedTo(actor);
        }
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        WorkOrder saved = repository.save(order);
        recordAudit("WORK_ORDER_STATUS_CHANGED", saved, actor, bearerToken);

        return toResponse(saved);
    }

    private void recordAudit(String type, WorkOrder order, String actor, String bearerToken) {
        try {
            auditEventClient.record(AuditEventFactory.create(type, order, actor), bearerToken);
        } catch (RuntimeException ex) {
            // Audit is best effort: never roll back a successfully persisted order.
            org.slf4j.LoggerFactory.getLogger(WorkOrderService.class)
                    .error("Could not record audit event for work order {}", order.getId(), ex);
        }
    }

    private boolean isTransitionAllowed(WorkOrderStatus current, WorkOrderStatus next) {
        if (current == next) return true;
        return switch (current) {
            case CREADA -> next == WorkOrderStatus.ASIGNADA || next == WorkOrderStatus.CANCELADA;
            case ASIGNADA -> next == WorkOrderStatus.EN_DESPLAZAMIENTO
                    || next == WorkOrderStatus.EN_EJECUCIÓN || next == WorkOrderStatus.CANCELADA;
            case EN_DESPLAZAMIENTO -> next == WorkOrderStatus.EN_EJECUCIÓN
                    || next == WorkOrderStatus.CANCELADA;
            case EN_EJECUCIÓN -> next == WorkOrderStatus.CERRADA || next == WorkOrderStatus.CANCELADA;
            case CERRADA, CANCELADA -> false;
        };
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
        response.setAssignedTo(order.getAssignedTo());
        response.setCreatedBy(order.getCreatedBy());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}
