package com.app.dashboard.controller;

import com.app.cliente.ClienteService;
import com.app.dashboard.dto.DashboardDto;
import com.app.producto.dto.ProductoDto;
import com.app.producto.service.ProductoService;
import com.app.venta.dto.VentaDto;
import com.app.venta.service.VentaService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller("/api/dashboard")
public class DashboardApiController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardApiController.class);

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final ClienteService clienteService;

    public DashboardApiController(VentaService ventaService,
                                  ProductoService productoService,
                                  ClienteService clienteService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.clienteService = clienteService;
    }

    @Get
    public DashboardDto getDashboardData() {
        DashboardDto dto = new DashboardDto();

        // 1. Obtener todas las ventas
        List<VentaDto> ventas = ventaService.findAll();
        
        LocalDateTime ahora = LocalDateTime.now();
        int mesActual = ahora.getMonthValue();
        int anioActual = ahora.getYear();

        BigDecimal totalVentasMes = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;
        int facturasEmitidasCount = 0;

        Map<String, BigDecimal> ventasPorFechaMap = new LinkedHashMap<>();
        DateTimeFormatter fmtDiaMes = DateTimeFormatter.ofPattern("dd/MM");

        for (VentaDto v : ventas) {
            if ("COMPLETADA".equalsIgnoreCase(v.getEstado())) {
                facturasEmitidasCount++;
                BigDecimal monto = v.getMontoTotal() != null ? v.getMontoTotal() : BigDecimal.ZERO;
                totalGeneral = totalGeneral.add(monto);

                if (v.getFecha() != null && v.getFecha().getMonthValue() == mesActual && v.getFecha().getYear() == anioActual) {
                    totalVentasMes = totalVentasMes.add(monto);
                } else if (v.getFecha() == null) {
                    totalVentasMes = totalVentasMes.add(monto);
                }

                String fechaStr = v.getFecha() != null ? v.getFecha().format(fmtDiaMes) : ahora.format(fmtDiaMes);
                ventasPorFechaMap.put(fechaStr, ventasPorFechaMap.getOrDefault(fechaStr, BigDecimal.ZERO).add(monto));
            }
        }

        if (totalVentasMes.compareTo(BigDecimal.ZERO) == 0 && !ventas.isEmpty()) {
            totalVentasMes = totalGeneral;
        }

        List<String> fechasList = new ArrayList<>(ventasPorFechaMap.keySet());
        List<BigDecimal> montosList = new ArrayList<>(ventasPorFechaMap.values());

        if (fechasList.isEmpty()) {
            fechasList.add(ahora.format(fmtDiaMes));
            montosList.add(BigDecimal.ZERO);
        }

        // 2. Productos e Inventario
        List<ProductoDto> productos = productoService.findAll();
        List<ProductoDto> productosBajoStockList = new ArrayList<>();

        for (ProductoDto p : productos) {
            int stock = p.getStock() != null ? p.getStock() : 0;
            if (stock <= 5) {
                productosBajoStockList.add(p);
            }
        }

        // 3. Clientes Registrados
        int totalClientesCount = clienteService.findAll().size();

        // Setear en DTO
        dto.setTotalVentasMes(totalVentasMes);
        dto.setFacturasEmitidasCount(facturasEmitidasCount);
        dto.setTotalProductosCount(productos.size());
        dto.setTotalClientesCount(totalClientesCount);
        dto.setFechasVentas(fechasList);
        dto.setMontosVentas(montosList);
        dto.setProductosBajoStockList(productosBajoStockList);

        LOG.info("=========================================");
        LOG.info("=== RETORNANDO JSON DESDE REST API ===");
        LOG.info("Ventas Mes: ₲ {}, Facturas: {}, Productos: {}, Clientes: {}", 
                dto.getTotalVentasMes(), dto.getFacturasEmitidasCount(), dto.getTotalProductosCount(), dto.getTotalClientesCount());
        LOG.info("Fechas del Gráfico: {}, Montos: {}", dto.getFechasVentas(), dto.getMontosVentas());
        LOG.info("Productos Bajo Stock: {} ítems", dto.getProductosBajoStockList().size());
        LOG.info("=========================================");

        return dto;
    }
}
