package com.digitalfix.workorders.domain;

public enum WorkOrderStatus {
    CREADA,
    ASIGNADA,
    EN_DESPLAZAMIENTO,
    EN_EJECUCIÓN,
    CERRADA,
    CANCELADA,
    /**
     * Legacy value kept so rows created by the previous version can be read
     * and normalized to CREADA when they are returned or updated.
     */
    PENDING
}
