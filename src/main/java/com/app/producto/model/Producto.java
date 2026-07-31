package com.app.producto.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.math.BigDecimal;

@MappedEntity
public class Producto {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String nombre;
    private String sku;
    private BigDecimal precio;
    private Integer stock;
    private String categoria;

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

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
