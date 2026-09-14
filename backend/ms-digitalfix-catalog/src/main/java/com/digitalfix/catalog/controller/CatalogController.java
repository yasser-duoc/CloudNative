package com.digitalfix.catalog.controller;

import com.digitalfix.catalog.domain.ServiceItem;
import com.digitalfix.catalog.domain.SparePart;
import com.digitalfix.catalog.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/services")
    public List<ServiceItem> listServices() {
        return service.listServices();
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceItem createService(@Valid @RequestBody ServiceItem serviceItem) {
        return service.createService(serviceItem);
    }

    @GetMapping("/spareparts")
    public List<SparePart> listSpareParts() {
        return service.listSpareParts();
    }

    @PostMapping("/spareparts")
    @ResponseStatus(HttpStatus.CREATED)
    public SparePart createSparePart(@Valid @RequestBody SparePart sparePart) {
        return service.createSparePart(sparePart);
    }

    @PatchMapping("/spareparts/{id}/stock")
    public SparePart updateStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        return service.updateSparePartStock(id, request.stock());
    }

    public record StockUpdateRequest(Integer stock) {
    }
}
