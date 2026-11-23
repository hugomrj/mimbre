package com.app.dashboard.dto;

import com.app.producto.dto.ProductoDto;
import com.app.venta.dto.VentaDto;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Serdeable
@Introspected
public class DashboardDto {

    // KPIs
    private BigDecimal totalVentasMes = BigDecimal.ZERO;
    private BigDecimal totalVentasHoy = BigDecimal.ZERO;
    private BigDecimal ticketPromedio = BigDecimal.ZERO;
    private BigDecimal totalComprasMes = BigDecimal.ZERO;
    private BigDecimal gananciaEstimadaMes = BigDecimal.ZERO;
    private BigDecimal valorInventario = BigDecimal.ZERO;
    private Integer facturasEmitidasCount = 0;
    private Integer facturasMesCount = 0;
    private Integer totalProductosCount = 0;
    private Integer totalClientesCount = 0;
    private Integer totalProveedoresCount = 0;
    private Integer productosBajoStockCount = 0;

    // Series
    private List<String> fechasVentas = new ArrayList<>();
    private List<BigDecimal> montosVentas = new ArrayList<>();
    private List<String> categoriasLabels = new ArrayList<>();
    private List<BigDecimal> categoriasMontos = new ArrayList<>();
    private List<String> topProductosLabels = new ArrayList<>();
    private List<Integer> topProductosCantidades = new ArrayList<>();
    private List<String> condicionesLabels = new ArrayList<>();
    private List<BigDecimal> condicionesMontos = new ArrayList<>();
    private List<String> mesesLabels = new ArrayList<>();
    private List<BigDecimal> ventasPorMes = new ArrayList<>();
    private List<BigDecimal> comprasPorMes = new ArrayList<>();

    private List<ProductoDto> productosBajoStockList = new ArrayList<>();
    private List<VentaDto> ultimasVentas = new ArrayList<>();

    public BigDecimal getTotalVentasMes() { return totalVentasMes; }
    public void setTotalVentasMes(BigDecimal totalVentasMes) { this.totalVentasMes = totalVentasMes; }

    public BigDecimal getTotalVentasHoy() { return totalVentasHoy; }
    public void setTotalVentasHoy(BigDecimal totalVentasHoy) { this.totalVentasHoy = totalVentasHoy; }

    public BigDecimal getTicketPromedio() { return ticketPromedio; }
    public void setTicketPromedio(BigDecimal ticketPromedio) { this.ticketPromedio = ticketPromedio; }

    public BigDecimal getTotalComprasMes() { return totalComprasMes; }
    public void setTotalComprasMes(BigDecimal totalComprasMes) { this.totalComprasMes = totalComprasMes; }

    public BigDecimal getGananciaEstimadaMes() { return gananciaEstimadaMes; }
    public void setGananciaEstimadaMes(BigDecimal gananciaEstimadaMes) { this.gananciaEstimadaMes = gananciaEstimadaMes; }

    public BigDecimal getValorInventario() { return valorInventario; }
    public void setValorInventario(BigDecimal valorInventario) { this.valorInventario = valorInventario; }

    public Integer getFacturasEmitidasCount() { return facturasEmitidasCount; }
    public void setFacturasEmitidasCount(Integer facturasEmitidasCount) { this.facturasEmitidasCount = facturasEmitidasCount; }

    public Integer getFacturasMesCount() { return facturasMesCount; }
    public void setFacturasMesCount(Integer facturasMesCount) { this.facturasMesCount = facturasMesCount; }

    public Integer getTotalProductosCount() { return totalProductosCount; }
    public void setTotalProductosCount(Integer totalProductosCount) { this.totalProductosCount = totalProductosCount; }

    public Integer getTotalClientesCount() { return totalClientesCount; }
    public void setTotalClientesCount(Integer totalClientesCount) { this.totalClientesCount = totalClientesCount; }

    public Integer getTotalProveedoresCount() { return totalProveedoresCount; }
    public void setTotalProveedoresCount(Integer totalProveedoresCount) { this.totalProveedoresCount = totalProveedoresCount; }

    public Integer getProductosBajoStockCount() { return productosBajoStockCount; }
    public void setProductosBajoStockCount(Integer productosBajoStockCount) { this.productosBajoStockCount = productosBajoStockCount; }

    public List<String> getFechasVentas() { return fechasVentas; }
    public void setFechasVentas(List<String> fechasVentas) { this.fechasVentas = fechasVentas; }

    public List<BigDecimal> getMontosVentas() { return montosVentas; }
    public void setMontosVentas(List<BigDecimal> montosVentas) { this.montosVentas = montosVentas; }

    public List<String> getCategoriasLabels() { return categoriasLabels; }
    public void setCategoriasLabels(List<String> categoriasLabels) { this.categoriasLabels = categoriasLabels; }

    public List<BigDecimal> getCategoriasMontos() { return categoriasMontos; }
    public void setCategoriasMontos(List<BigDecimal> categoriasMontos) { this.categoriasMontos = categoriasMontos; }

    public List<String> getTopProductosLabels() { return topProductosLabels; }
    public void setTopProductosLabels(List<String> topProductosLabels) { this.topProductosLabels = topProductosLabels; }

    public List<Integer> getTopProductosCantidades() { return topProductosCantidades; }
    public void setTopProductosCantidades(List<Integer> topProductosCantidades) { this.topProductosCantidades = topProductosCantidades; }

    public List<String> getCondicionesLabels() { return condicionesLabels; }
    public void setCondicionesLabels(List<String> condicionesLabels) { this.condicionesLabels = condicionesLabels; }

    public List<BigDecimal> getCondicionesMontos() { return condicionesMontos; }
    public void setCondicionesMontos(List<BigDecimal> condicionesMontos) { this.condicionesMontos = condicionesMontos; }

    public List<String> getMesesLabels() { return mesesLabels; }
    public void setMesesLabels(List<String> mesesLabels) { this.mesesLabels = mesesLabels; }

    public List<BigDecimal> getVentasPorMes() { return ventasPorMes; }
    public void setVentasPorMes(List<BigDecimal> ventasPorMes) { this.ventasPorMes = ventasPorMes; }

    public List<BigDecimal> getComprasPorMes() { return comprasPorMes; }
    public void setComprasPorMes(List<BigDecimal> comprasPorMes) { this.comprasPorMes = comprasPorMes; }

    public List<ProductoDto> getProductosBajoStockList() { return productosBajoStockList; }
    public void setProductosBajoStockList(List<ProductoDto> productosBajoStockList) { this.productosBajoStockList = productosBajoStockList; }

    public List<VentaDto> getUltimasVentas() { return ultimasVentas; }
    public void setUltimasVentas(List<VentaDto> ultimasVentas) { this.ultimasVentas = ultimasVentas; }
}
