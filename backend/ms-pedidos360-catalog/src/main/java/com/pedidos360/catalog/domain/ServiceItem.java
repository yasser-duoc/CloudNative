package com.pedidos360.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "CATALOG_SERVICES")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_item_seq")
    @SequenceGenerator(name = "service_item_seq", sequenceName = "SERVICE_ITEM_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "CATEGORY", length = 80)
    private String category;

    @Column(name = "UNIT_PRICE", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
