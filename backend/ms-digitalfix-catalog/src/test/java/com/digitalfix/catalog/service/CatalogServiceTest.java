package com.digitalfix.catalog.service;

import com.digitalfix.catalog.domain.ServiceItem;
import com.digitalfix.catalog.domain.SparePart;
import com.digitalfix.catalog.repository.ServiceItemRepository;
import com.digitalfix.catalog.repository.SparePartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ServiceItemRepository serviceItemRepository;

    @Mock
    private SparePartRepository sparePartRepository;

    @InjectMocks
    private CatalogService service;

    @Test
    void listServicesReturnsOnlyActiveServices() {
        ServiceItem active = new ServiceItem();
        active.setId(1L);
        active.setName("Mantencion");
        active.setActive(true);
        ServiceItem inactive = new ServiceItem();
        inactive.setId(2L);
        inactive.setName("Garantia");
        inactive.setActive(false);

        when(serviceItemRepository.findByActiveTrue()).thenReturn(List.of(active));

        List<ServiceItem> result = service.listServices();

        assertThat(result).hasSize(1).contains(active);
        assertThat(result).doesNotContain(inactive);
    }

    @Test
    void createServicePersistsItem() {
        ServiceItem item = new ServiceItem();
        item.setName("Mantencion");
        item.setCategory("SERVICIO");
        item.setUnitPrice(BigDecimal.valueOf(1000.00));

        when(serviceItemRepository.save(any(ServiceItem.class))).thenAnswer(invocation -> {
            ServiceItem saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ServiceItem result = service.createService(item);

        assertThat(result.getId()).isEqualTo(1L);
        verify(serviceItemRepository).save(item);
    }

    @Test
    void createSparePartPersistsPart() {
        SparePart part = new SparePart();
        part.setName("Disco NVMe 512GB");
        part.setSku("NV512");

        when(sparePartRepository.save(any(SparePart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createSparePart(part);

        verify(sparePartRepository).save(part);
    }

    @Test
    void updateSparePartStockUpdatesValue() {
        SparePart part = new SparePart();
        part.setId(3L);
        part.setStock(5);

        when(sparePartRepository.findById(3L)).thenReturn(Optional.of(part));
        when(sparePartRepository.save(any(SparePart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SparePart updated = service.updateSparePartStock(3L, 12);

        assertThat(updated.getStock()).isEqualTo(12);
    }

    @Test
    void updateSparePartStockThrowsNotFoundWhenMissing() {
        when(sparePartRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSparePartStock(99L, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}