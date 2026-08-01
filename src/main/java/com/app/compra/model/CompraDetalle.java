package com.app.compra.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.math.BigDecimal;

@MappedEntity
public class CompraDetalle {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private Long compraId;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioCosto;
    private BigDecimal subtotal;

    public CompraDetalle() {
    }

    public CompraDetalle(Long compraId, Long productoId, String productoNombre, Integer cantidad, BigDecimal precioCosto, BigDecimal subtotal) {
        this.compraId = compraId;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioCosto = precioCosto;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompraId() { return compraId; }
    public void setCompraId(Long compraId) { this.compraId = compraId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal precioCosto) { this.precioCosto = precioCosto; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
