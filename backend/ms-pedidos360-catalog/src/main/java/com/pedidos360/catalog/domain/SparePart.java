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
@Table(name = "SPARE_PARTS")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spare_part_seq")
    @SequenceGenerator(name = "spare_part_seq", sequenceName = "SPARE_PART_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "SKU", nullable = false, length = 50, unique = true)
    private String sku;

    @Column(name = "STOCK", nullable = false)
    private Integer stock = 0;

    @Column(name = "UNIT_PRICE", precision = 12, scale = 2)
    private BigDecimal unitPrice;

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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
