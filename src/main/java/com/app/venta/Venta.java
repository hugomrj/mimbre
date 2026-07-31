package com.app.venta;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedEntity
public class Venta {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String numeroFactura;
    private Long clienteId;
    private String clienteNombre;
    private LocalDateTime fecha;
    private String condicion;
    private BigDecimal montoTotal;
    private String estado;

    public Venta() {
    }

    public Venta(String numeroFactura, Long clienteId, String clienteNombre, LocalDateTime fecha, String condicion, BigDecimal montoTotal, String estado) {
        this.numeroFactura = numeroFactura;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.fecha = fecha;
        this.condicion = condicion;
        this.montoTotal = montoTotal;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal montoTotal) { this.montoTotal = montoTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
