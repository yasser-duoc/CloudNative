package com.digitalfix.workorders.service;

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

@Service
@Transactional
public class WorkOrderService {

    private final WorkOrderRepository repository;
    public WorkOrderService(WorkOrderRepository repository) {
        this.repository = repository;
    }

    public WorkOrderResponse create(WorkOrderRequest request, String createdBy) {
        WorkOrder order = new WorkOrder();
        order.setCustomerName(request.getCustomerName());
        order.setServiceId(request.getServiceId());
        order.setDescription(request.getDescription());
        order.setStatus(WorkOrderStatus.PENDING);
        order.setCreatedBy(createdBy);

        WorkOrder saved = repository.save(order);

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
        WorkOrder saved = repository.save(order);

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
