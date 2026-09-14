package com.digitalfix.workorders.controller;

import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.domain.dto.WorkOrderRequest;
import com.digitalfix.workorders.domain.dto.WorkOrderResponse;
import com.digitalfix.workorders.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderService service;

    public WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.create(request, jwt.getClaimAsString("preferred_username"));
    }

    @GetMapping
    public List<WorkOrderResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public WorkOrderResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}/status")
    public WorkOrderResponse updateStatus(@PathVariable Long id,
                                          @RequestBody StatusUpdateRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        return service.updateStatus(id, request.status(), jwt.getClaimAsString("preferred_username"));
    }

    public record StatusUpdateRequest(WorkOrderStatus status) {
    }
}
