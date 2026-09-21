package com.pedidos360.workorders.repository;

import com.pedidos360.workorders.domain.WorkOrder;
import com.pedidos360.workorders.domain.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    List<WorkOrder> findByStatus(WorkOrderStatus status);
}
