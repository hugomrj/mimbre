package com.app.producto.model;

import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedEntity
public class Producto {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    @DateCreated
    private LocalDateTime creadoEn;

    @DateUpdated
    private LocalDateTime actualizadoEn;

    private boolean activo = true;

    private String nombre;
    private String sku;
    private BigDecimal precio;
    private Integer stock;
    private Long categoriaId;
    private String categoria;
    private BigDecimal costoActual;
    private Integer stockMinimo;

    public Producto() {
    }

    public Producto(String nombre, String sku, BigDecimal precio, Integer stock, String categoria) {
        this.nombre = nombre;
        this.sku = sku;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getCostoActual() { return costoActual; }
    public void setCostoActual(BigDecimal costoActual) { this.costoActual = costoActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
