package com.app.producto.dto;

import io.micronaut.serde.annotation.Serdeable;
import com.app.utils.FormatoNumero;

import java.math.BigDecimal;

@Serdeable
public class ProductoDto {
    private Long id;
    private String nombre;
    private String sku;
    private BigDecimal precioVenta;
    private BigDecimal precioCompra;
    private Integer stock;
    private Long categoriaId;
    private String categoria;
    private BigDecimal costoActual;
    private Integer stockMinimo;

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

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }

    public String getPrecioVentaMiles() {
        return FormatoNumero.miles(precioVenta);
    }

    public String getPrecioCompraMiles() {
        return FormatoNumero.miles(precioCompra);
    }


    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
