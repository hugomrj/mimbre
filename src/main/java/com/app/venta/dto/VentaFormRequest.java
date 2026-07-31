package com.app.venta.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Serdeable
@Introspected
public class VentaFormRequest {

    private Long clienteId;
    private String condicion;
    private List<Long> productoId = new ArrayList<>();
    private List<Integer> cantidad = new ArrayList<>();
    private List<BigDecimal> precioUnitario = new ArrayList<>();

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public List<Long> getProductoId() {
        return productoId;
    }

    public void setProductoId(List<Long> productoId) {
        this.productoId = productoId;
    }

    public List<Integer> getCantidad() {
        return cantidad;
    }

    public void setCantidad(List<Integer> cantidad) {
        this.cantidad = cantidad;
    }

    public List<BigDecimal> getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(List<BigDecimal> precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    @Override
    public String toString() {
        return "VentaFormRequest{" +
                "clienteId=" + clienteId +
                ", condicion='" + condicion + '\'' +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                '}';
    }
}
