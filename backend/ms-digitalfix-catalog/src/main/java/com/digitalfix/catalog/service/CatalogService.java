package com.digitalfix.catalog.service;

import com.digitalfix.catalog.domain.ServiceItem;
import com.digitalfix.catalog.domain.SparePart;
import com.digitalfix.catalog.repository.ServiceItemRepository;
import com.digitalfix.catalog.repository.SparePartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CatalogService {

    private final ServiceItemRepository serviceItemRepository;
    private final SparePartRepository sparePartRepository;

    public CatalogService(ServiceItemRepository serviceItemRepository,
                          SparePartRepository sparePartRepository) {
        this.serviceItemRepository = serviceItemRepository;
        this.sparePartRepository = sparePartRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceItem> listServices() {
        return serviceItemRepository.findByActiveTrue();
    }

    public ServiceItem createService(ServiceItem service) {
        return serviceItemRepository.save(service);
    }

    @Transactional(readOnly = true)
    public List<SparePart> listSpareParts() {
        return sparePartRepository.findByActiveTrue();
    }

    public SparePart createSparePart(SparePart sparePart) {
        return sparePartRepository.save(sparePart);
    }

    public SparePart updateSparePartStock(Long id, Integer stock) {
        SparePart sparePart = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Spare part not found: " + id));
        sparePart.setStock(stock);
        return sparePartRepository.save(sparePart);
    }
}
