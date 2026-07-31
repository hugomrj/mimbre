package com.app.dashboard.dto;

import com.app.producto.ProductoDto;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Serdeable
@Introspected
public class DashboardDto {

    private BigDecimal totalVentasMes = BigDecimal.ZERO;
    private Integer facturasEmitidasCount = 0;
    private Integer totalProductosCount = 0;
    private Integer totalClientesCount = 0;
    private List<String> fechasVentas = new ArrayList<>();
    private List<BigDecimal> montosVentas = new ArrayList<>();
    private List<ProductoDto> productosBajoStockList = new ArrayList<>();

    public BigDecimal getTotalVentasMes() {
        return totalVentasMes;
    }

    public void setTotalVentasMes(BigDecimal totalVentasMes) {
        this.totalVentasMes = totalVentasMes;
    }

    public Integer getFacturasEmitidasCount() {
        return facturasEmitidasCount;
    }

    public void setFacturasEmitidasCount(Integer facturasEmitidasCount) {
        this.facturasEmitidasCount = facturasEmitidasCount;
    }

    public Integer getTotalProductosCount() {
        return totalProductosCount;
    }

    public void setTotalProductosCount(Integer totalProductosCount) {
        this.totalProductosCount = totalProductosCount;
    }

    public Integer getTotalClientesCount() {
        return totalClientesCount;
    }

    public void setTotalClientesCount(Integer totalClientesCount) {
        this.totalClientesCount = totalClientesCount;
    }

    public List<String> getFechasVentas() {
        return fechasVentas;
    }

    public void setFechasVentas(List<String> fechasVentas) {
        this.fechasVentas = fechasVentas;
    }

    public List<BigDecimal> getMontosVentas() {
        return montosVentas;
    }

    public void setMontosVentas(List<BigDecimal> montosVentas) {
        this.montosVentas = montosVentas;
    }

    public List<ProductoDto> getProductosBajoStockList() {
        return productosBajoStockList;
    }

    public void setProductosBajoStockList(List<ProductoDto> productosBajoStockList) {
        this.productosBajoStockList = productosBajoStockList;
    }
}
