package com.app.dashboard.controller;

import com.app.cliente.service.ClienteService;
import com.app.compra.dto.CompraDto;
import com.app.compra.service.CompraService;
import com.app.dashboard.dto.DashboardDto;
import com.app.producto.dto.ProductoDto;
import com.app.producto.service.ProductoService;
import com.app.proveedor.ProveedorService;
import com.app.venta.dto.VentaDetalleDto;
import com.app.venta.dto.VentaDto;
import com.app.venta.service.VentaService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/api/dashboard")
public class DashboardApiController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardApiController.class);
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MES_ANIO = DateTimeFormatter.ofPattern("MM/yyyy");

    private final VentaService ventaService;
    private final CompraService compraService;
    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final ProveedorService proveedorService;

    public DashboardApiController(VentaService ventaService,
                                  CompraService compraService,
                                  ProductoService productoService,
                                  ClienteService clienteService,
                                  ProveedorService proveedorService) {
        this.ventaService = ventaService;
        this.compraService = compraService;
        this.productoService = productoService;
        this.clienteService = clienteService;
        this.proveedorService = proveedorService;
    }

    @Get
    public DashboardDto getDashboardData() {
        DashboardDto dto = new DashboardDto();

        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();
        YearMonth mesActual = YearMonth.from(ahora);

        List<VentaDto> ventas = ventaService.findAllConDetalles();
        List<CompraDto> compras = compraService.findAll();
        List<ProductoDto> productos = productoService.findAll();

        Map<Long, ProductoDto> productoPorId = productos.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(ProductoDto::getId, p -> p, (a, b) -> a));

        BigDecimal totalVentasMes = BigDecimal.ZERO;
        BigDecimal totalVentasHoy = BigDecimal.ZERO;
        BigDecimal gananciaMes = BigDecimal.ZERO;
        int facturasTodas = 0;
        int facturasMes = 0;

        Map<LocalDate, BigDecimal> ventasPorDia = new HashMap<>();
        Map<String, BigDecimal> ventasPorCategoria = new HashMap<>();
        Map<String, Integer> cantidadesPorProducto = new HashMap<>();
        Map<String, BigDecimal> ventasPorCondicion = new LinkedHashMap<>();
        ventasPorCondicion.put("Contado", BigDecimal.ZERO);
        ventasPorCondicion.put("Crédito", BigDecimal.ZERO);

        for (VentaDto venta : ventas) {
            if (!"COMPLETADA".equalsIgnoreCase(venta.getEstado())) {
                continue;
            }

            BigDecimal monto = venta.getMontoTotal() != null ? venta.getMontoTotal() : BigDecimal.ZERO;
            facturasTodas++;
            LocalDate fecha = venta.getFecha() != null ? venta.getFecha().toLocalDate() : null;

            if (fecha != null) {
                ventasPorDia.merge(fecha, monto, BigDecimal::add);
                if (fecha.equals(hoy)) {
                    totalVentasHoy = totalVentasHoy.add(monto);
                }
            }

            if (fecha == null || !YearMonth.from(fecha).equals(mesActual)) {
                continue;
            }

            facturasMes++;
            totalVentasMes = totalVentasMes.add(monto);

            String condicion = venta.getCondicion() != null ? venta.getCondicion() : "Contado";
            ventasPorCondicion.merge(condicion, monto, BigDecimal::add);

            for (VentaDetalleDto detalle : venta.getDetalles()) {
                ProductoDto producto = detalle.getProductoId() != null
                        ? productoPorId.get(detalle.getProductoId()) : null;

                String categoria = producto != null && producto.getCategoria() != null
                        ? producto.getCategoria() : "Sin categoría";
                BigDecimal subtotal = detalle.getSubtotal() != null ? detalle.getSubtotal() : BigDecimal.ZERO;
                ventasPorCategoria.merge(categoria, subtotal, BigDecimal::add);

                String nombre = detalle.getProductoNombre() != null ? detalle.getProductoNombre() : "Producto";
                int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                cantidadesPorProducto.merge(nombre, cantidad, Integer::sum);

                if (producto != null && cantidad > 0) {
                    BigDecimal costo = producto.getCostoActual() != null ? producto.getCostoActual() : BigDecimal.ZERO;
                    BigDecimal precio = detalle.getPrecioUnitario() != null ? detalle.getPrecioUnitario() : BigDecimal.ZERO;
                    gananciaMes = gananciaMes.add(
                            precio.subtract(costo).multiply(BigDecimal.valueOf(cantidad)));
                }
            }
        }

        // Últimos 14 días, incluyendo días sin ventas.
        List<String> fechasVentas = new ArrayList<>();
        List<BigDecimal> montosVentas = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            fechasVentas.add(dia.format(DIA_MES));
            montosVentas.add(ventasPorDia.getOrDefault(dia, BigDecimal.ZERO));
        }

        // Ventas por categoría, de mayor a menor.
        List<Map.Entry<String, BigDecimal>> categorias = ventasPorCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .toList();
        if (categorias.size() > 5) {
            BigDecimal otros = categorias.subList(5, categorias.size()).stream()
                    .map(Map.Entry::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            categorias = new ArrayList<>(categorias.subList(0, 5));
            categorias.add(Map.entry("Otros", otros));
        }
        dto.setCategoriasLabels(categorias.stream().map(Map.Entry::getKey).toList());
        dto.setCategoriasMontos(categorias.stream().map(Map.Entry::getValue).toList());

        // Top 5 productos por cantidad vendida en el mes.
        List<Map.Entry<String, Integer>> topProductos = cantidadesPorProducto.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();
        dto.setTopProductosLabels(topProductos.stream().map(Map.Entry::getKey).toList());
        dto.setTopProductosCantidades(topProductos.stream().map(Map.Entry::getValue).toList());

        dto.setCondicionesLabels(new ArrayList<>(ventasPorCondicion.keySet()));
        dto.setCondicionesMontos(new ArrayList<>(ventasPorCondicion.values()));

        // Compras del mes.
        BigDecimal totalComprasMes = BigDecimal.ZERO;
        Map<YearMonth, BigDecimal> ventasPorMesMap = new HashMap<>();
        Map<YearMonth, BigDecimal> comprasPorMesMap = new HashMap<>();

        for (VentaDto venta : ventas) {
            if ("COMPLETADA".equalsIgnoreCase(venta.getEstado()) && venta.getFecha() != null) {
                ventasPorMesMap.merge(YearMonth.from(venta.getFecha()), venta.getMontoTotal() != null
                        ? venta.getMontoTotal() : BigDecimal.ZERO, BigDecimal::add);
            }
        }
        for (CompraDto compra : compras) {
            if (!"COMPLETADA".equalsIgnoreCase(compra.getEstado()) || compra.getFecha() == null) {
                continue;
            }
            BigDecimal monto = compra.getMontoTotal() != null ? compra.getMontoTotal() : BigDecimal.ZERO;
            YearMonth ym = YearMonth.from(compra.getFecha());
            comprasPorMesMap.merge(ym, monto, BigDecimal::add);
            if (ym.equals(mesActual)) {
                totalComprasMes = totalComprasMes.add(monto);
            }
        }

        // Últimos 6 meses.
        List<String> mesesLabels = new ArrayList<>();
        List<BigDecimal> ventasPorMes = new ArrayList<>();
        List<BigDecimal> comprasPorMes = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = mesActual.minusMonths(i);
            mesesLabels.add(ym.format(MES_ANIO));
            ventasPorMes.add(ventasPorMesMap.getOrDefault(ym, BigDecimal.ZERO));
            comprasPorMes.add(comprasPorMesMap.getOrDefault(ym, BigDecimal.ZERO));
        }

        // Inventario y bajo stock.
        BigDecimal valorInventario = BigDecimal.ZERO;
        List<ProductoDto> productosBajoStock = new ArrayList<>();
        for (ProductoDto producto : productos) {
            int stock = producto.getStock() != null ? producto.getStock() : 0;
            BigDecimal costo = producto.getCostoActual() != null ? producto.getCostoActual() : BigDecimal.ZERO;
            valorInventario = valorInventario.add(costo.multiply(BigDecimal.valueOf(stock)));

            int minimo = producto.getStockMinimo() != null ? producto.getStockMinimo() : 5;
            if (stock <= minimo) {
                productosBajoStock.add(producto);
            }
        }

        dto.setTotalVentasMes(totalVentasMes);
        dto.setTotalVentasHoy(totalVentasHoy);
        dto.setTicketPromedio(facturasMes > 0
                ? totalVentasMes.divide(BigDecimal.valueOf(facturasMes), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        dto.setTotalComprasMes(totalComprasMes);
        dto.setGananciaEstimadaMes(gananciaMes);
        dto.setValorInventario(valorInventario);
        dto.setFacturasEmitidasCount(facturasTodas);
        dto.setFacturasMesCount(facturasMes);
        dto.setTotalProductosCount(productos.size());
        dto.setTotalClientesCount(clienteService.findAll().size());
        dto.setTotalProveedoresCount(proveedorService.findAll().size());
        dto.setProductosBajoStockCount(productosBajoStock.size());
        dto.setFechasVentas(fechasVentas);
        dto.setMontosVentas(montosVentas);
        dto.setMesesLabels(mesesLabels);
        dto.setVentasPorMes(ventasPorMes);
        dto.setComprasPorMes(comprasPorMes);
        dto.setProductosBajoStockList(productosBajoStock);
        dto.setUltimasVentas(ventas.stream().limit(5).collect(Collectors.toList()));

        LOG.info("Dashboard: ventas mes ₲ {}, hoy ₲ {}, compras mes ₲ {}, facturas {}, bajo stock {}",
                totalVentasMes, totalVentasHoy, totalComprasMes, facturasMes, productosBajoStock.size());

        return dto;
    }
}
