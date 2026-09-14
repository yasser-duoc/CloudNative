package com.digitalfix.workorders.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WorkOrderRequest {

    @NotBlank(message = "customerName es obligatorio")
    @Size(max = 120)
    private String customerName;

    @NotNull(message = "serviceId es obligatorio")
    private Long serviceId;

    @Size(max = 2000)
    private String description;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
