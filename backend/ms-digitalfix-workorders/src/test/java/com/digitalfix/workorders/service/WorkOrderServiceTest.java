package com.digitalfix.workorders.service;

import com.digitalfix.workorders.domain.WorkOrder;
import com.digitalfix.workorders.domain.WorkOrderStatus;
import com.digitalfix.workorders.domain.dto.WorkOrderRequest;
import com.digitalfix.workorders.domain.dto.WorkOrderResponse;
import com.digitalfix.workorders.event.WorkOrderEvent;
import com.digitalfix.workorders.publisher.WorkOrderEventPublisher;
import com.digitalfix.workorders.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository repository;

    @Mock
    private WorkOrderEventPublisher publisher;

    @InjectMocks
    private WorkOrderService service;

    @Test
    void createPersistsAndPublishesEvent() {
        WorkOrderRequest request = new WorkOrderRequest();
        request.setCustomerName("Cliente Test");
        request.setServiceId(1L);
        request.setDescription("Mantención preventiva");

        WorkOrder saved = new WorkOrder();
        saved.setId(10L);
        saved.setCustomerName("Cliente Test");
        saved.setServiceId(1L);
        saved.setDescription("Mantención preventiva");
        saved.setStatus(WorkOrderStatus.PENDING);

        when(repository.save(any(WorkOrder.class))).thenReturn(saved);

        WorkOrderResponse response = service.create(request, "tester@digitalfix.cl");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.PENDING);
        verify(repository).save(any(WorkOrder.class));
        verify(publisher).publish(any(WorkOrderEvent.class));
    }
}
