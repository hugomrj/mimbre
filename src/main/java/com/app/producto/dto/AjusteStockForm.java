package com.app.producto.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class AjusteStockForm {
    private Long productoId;
    private Integer cantidad;
    private String motivo;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
