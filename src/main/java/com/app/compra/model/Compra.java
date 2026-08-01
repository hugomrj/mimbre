package com.app.compra.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedEntity
public class Compra {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String numeroFactura;
    private Long proveedorId;
    private String proveedorNombre;
    private LocalDateTime fecha;
    private String condicion;
    private BigDecimal montoTotal;
    private String estado;
    @Nullable
    private LocalDateTime fechaAnulacion;

    public Compra() {
    }

    public Compra(String numeroFactura, Long proveedorId, String proveedorNombre, LocalDateTime fecha, String condicion, BigDecimal montoTotal, String estado) {
        this.numeroFactura = numeroFactura;
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.fecha = fecha;
        this.condicion = condicion;
        this.montoTotal = montoTotal;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime fechaAnulacion) { this.fechaAnulacion = fechaAnulacion; }
}
