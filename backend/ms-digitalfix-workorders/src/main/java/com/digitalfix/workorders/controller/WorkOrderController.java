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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.time.Instant;

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
                                    @AuthenticationPrincipal Jwt jwt,
                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.create(request, jwt.getClaimAsString("preferred_username"), authorization);
    }

    @GetMapping
    public List<WorkOrderResponse> findAll(@RequestParam(required = false) WorkOrderStatus status,
                                           @RequestParam(required = false) Instant from,
                                           @RequestParam(required = false) Instant to) {
        return service.findAll(status, from, to);
    }

    @GetMapping("/{id}")
    public WorkOrderResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}/status")
    public WorkOrderResponse updateStatus(@PathVariable Long id,
                                          @RequestBody StatusUpdateRequest request,
                                          @AuthenticationPrincipal Jwt jwt,
                                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.updateStatus(id, request.status(), jwt.getClaimAsString("preferred_username"), authorization);
    }

    public record StatusUpdateRequest(WorkOrderStatus status) {
    }
}
