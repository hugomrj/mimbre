package com.app.reporte.service;

import com.app.compra.dto.CompraDetalleDto;
import com.app.compra.dto.CompraDto;
import com.app.compra.service.CompraService;
import com.app.exception.BusinessException;
import com.app.movimiento_stock.dto.MovimientoStockDto;
import com.app.movimiento_stock.service.MovimientoStockService;
import com.app.producto.dto.ProductoDto;
import com.app.producto.service.ProductoService;
import com.app.reporte.dto.ReporteDto;
import com.app.utils.FormatoNumero;
import com.app.venta.dto.VentaDetalleDto;
import com.app.venta.dto.VentaDto;
import com.app.venta.service.VentaService;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ReporteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VentaService ventaService;
    private final CompraService compraService;
    private final ProductoService productoService;
    private final MovimientoStockService movimientoStockService;

    public ReporteService(VentaService ventaService,
                          CompraService compraService,
                          ProductoService productoService,
                          MovimientoStockService movimientoStockService) {
        this.ventaService = ventaService;
        this.compraService = compraService;
        this.productoService = productoService;
        this.movimientoStockService = movimientoStockService;
    }

    public ReporteDto construir(String tipo, LocalDateTime desde, LocalDateTime hasta) {
        return switch (tipo) {
            case "resumen" -> resumen(desde, hasta);
            case "ventas" -> ventas(desde, hasta);
            case "ventas-producto" -> ventasProducto(desde, hasta);
            case "ventas-cliente" -> ventasCliente(desde, hasta);
            case "compras" -> compras(desde, hasta);
            case "compras-proveedor" -> comprasProveedor(desde, hasta);
            case "inventario" -> inventario();
            case "movimientos" -> movimientos(desde, hasta);
            case "anulaciones" -> anulaciones(desde, hasta);
            case "sin-movimiento" -> sinMovimiento(desde, hasta);
            case "margen" -> margen(desde, hasta);
            default -> throw new BusinessException("Reporte no encontrado: " + tipo);
        };
    }

    private ReporteDto resumen(LocalDateTime desde, LocalDateTime hasta) {
        BigDecimal ventasTotal = ventaService.findAll().stream()
                .filter(v -> enRango(v.getFecha(), desde, hasta))
                .filter(v -> !"ANULADA".equalsIgnoreCase(v.getEstado()))
                .map(v -> nz(v.getMontoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal comprasTotal = compraService.findAll().stream()
                .filter(c -> enRango(c.getFecha(), desde, hasta))
                .filter(c -> !"ANULADA".equalsIgnoreCase(c.getEstado()))
                .map(c -> nz(c.getMontoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ProductoDto> productos = productoService.findAll();
        BigDecimal inventario = BigDecimal.ZERO;
        long bajoStock = 0;
        for (ProductoDto p : productos) {
            int stock = p.getStock() == null ? 0 : p.getStock();
            inventario = inventario.add(nz(p.getCostoActual()).multiply(BigDecimal.valueOf(stock)));
            if (stock <= (p.getStockMinimo() == null ? 5 : p.getStockMinimo())) bajoStock++;
        }

        Map<Long, Acumulado> acumulado = acumularProductos(
                ventaService.findAllConDetalles().stream()
                        .filter(v -> !"ANULADA".equalsIgnoreCase(v.getEstado()))
                        .filter(v -> enRango(v.getFecha(), desde, hasta))
                        .toList(), productosPorId());
        BigDecimal costo = acumulado.values().stream().map(a -> a.costo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal margen = ventasTotal.subtract(costo);

        ReporteDto r = base("resumen", "Resumen ejecutivo", "Indicadores principales del período");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Indicador", "Valor"));
        r.fila("Ventas del período", money(ventasTotal));
        r.fila("Compras del período", money(comprasTotal));
        r.fila("Margen estimado", money(margen));
        r.fila("Inventario a costo", money(inventario));
        r.fila("Productos", String.valueOf(productos.size()));
        r.fila("Productos bajo stock", String.valueOf(bajoStock));
        r.kpi("Ventas", money(ventasTotal));
        r.kpi("Compras", money(comprasTotal));
        r.kpi("Margen estimado", money(margen));
        r.kpi("Inventario a costo", money(inventario));
        return r;
    }

    private ReporteDto ventas(LocalDateTime desde, LocalDateTime hasta) {
        List<VentaDto> ventas = ventaService.findAll().stream()
                .filter(v -> enRango(v.getFecha(), desde, hasta))
                .toList();
        BigDecimal total = BigDecimal.ZERO;
        long contado = 0;
        long credito = 0;

        ReporteDto r = base("ventas", "Ventas por período", "Facturas emitidas en el rango seleccionado");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Factura", "Fecha", "Cliente", "Condición", "Total", "Estado"));
        for (VentaDto v : ventas) {
            BigDecimal monto = nz(v.getMontoTotal());
            total = total.add(monto);
            if ("Contado".equalsIgnoreCase(v.getCondicion())) contado++;
            if ("Crédito".equalsIgnoreCase(v.getCondicion())) credito++;
            r.fila(s(v.getNumeroFactura()), fechaHora(v.getFecha()), s(v.getClienteNombre()),
                    s(v.getCondicion()), money(monto), s(v.getEstado()));
        }
        r.kpi("Total vendido", money(total));
        r.kpi("Facturas", String.valueOf(ventas.size()));
        r.kpi("Ticket promedio", money(promedio(total, ventas.size())));
        r.kpi("Contado / Crédito", contado + " / " + credito);
        return r;
    }

    private ReporteDto ventasProducto(LocalDateTime desde, LocalDateTime hasta) {
        Map<Long, ProductoDto> productos = productosPorId();
        Map<Long, Acumulado> acumulado = acumularProductos(
                ventaService.findAllConDetalles().stream()
                        .filter(v -> !"ANULADA".equalsIgnoreCase(v.getEstado()))
                        .filter(v -> enRango(v.getFecha(), desde, hasta))
                        .toList(), productos);

        ReporteDto r = base("ventas-producto", "Ventas por producto",
                "Unidades, ingresos y margen estimado por producto");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Producto", "Unidades", "Ingresos", "Costo actual", "Margen"));
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal costo = BigDecimal.ZERO;
        long unidades = 0;
        for (Acumulado a : ordenarPorIngreso(acumulado)) {
            ingresos = ingresos.add(a.ingresos);
            costo = costo.add(a.costo);
            unidades += a.unidades;
            r.fila(a.nombre, String.valueOf(a.unidades), money(a.ingresos), money(a.costo),
                    money(a.ingresos.subtract(a.costo)));
        }
        r.kpi("Ingresos", money(ingresos));
        r.kpi("Unidades", String.valueOf(unidades));
        r.kpi("Costo estimado", money(costo));
        r.kpi("Margen estimado", money(ingresos.subtract(costo)));
        return r;
    }

    private ReporteDto ventasCliente(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, ClienteAcumulado> acumulado = new LinkedHashMap<>();
        for (VentaDto v : ventaService.findAll()) {
            if (!enRango(v.getFecha(), desde, hasta) || "ANULADA".equalsIgnoreCase(v.getEstado())) continue;
            String cliente = s(v.getClienteNombre()).isBlank() ? "Consumidor final" : s(v.getClienteNombre());
            ClienteAcumulado a = acumulado.computeIfAbsent(cliente, k -> new ClienteAcumulado());
            a.nombre = cliente;
            a.facturas++;
            a.total = a.total.add(nz(v.getMontoTotal()));
            if (a.ultima == null || (v.getFecha() != null && v.getFecha().isAfter(a.ultima))) a.ultima = v.getFecha();
        }

        ReporteDto r = base("ventas-cliente", "Ventas por cliente", "Ranking de clientes del período");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Cliente", "Facturas", "Total comprado", "Ticket promedio", "Última venta"));
        for (ClienteAcumulado a : acumulado.values().stream()
                .sorted((x, y) -> y.total.compareTo(x.total)).toList()) {
            r.fila(a.nombre, String.valueOf(a.facturas), money(a.total),
                    money(promedio(a.total, a.facturas)), a.ultima == null ? "-" : fecha(a.ultima));
        }
        r.kpi("Clientes", String.valueOf(acumulado.size()));
        r.kpi("Facturas", String.valueOf(acumulado.values().stream().mapToLong(a -> a.facturas).sum()));
        r.kpi("Total vendido", money(acumulado.values().stream().map(a -> a.total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        return r;
    }

    private ReporteDto compras(LocalDateTime desde, LocalDateTime hasta) {
        List<CompraDto> compras = compraService.findAll().stream()
                .filter(c -> enRango(c.getFecha(), desde, hasta))
                .toList();
        BigDecimal total = BigDecimal.ZERO;

        ReporteDto r = base("compras", "Compras por período", "Facturas de compra del rango seleccionado");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Factura", "Fecha", "Proveedor", "Condición", "Total", "Estado"));
        for (CompraDto c : compras) {
            BigDecimal monto = nz(c.getMontoTotal());
            total = total.add(monto);
            r.fila(s(c.getNumeroFactura()), fechaHora(c.getFecha()), s(c.getProveedorNombre()),
                    s(c.getCondicion()), money(monto), s(c.getEstado()));
        }
        r.kpi("Total comprado", money(total));
        r.kpi("Facturas", String.valueOf(compras.size()));
        r.kpi("Ticket promedio", money(promedio(total, compras.size())));
        r.kpi("Completadas", String.valueOf(compras.stream()
                .filter(c -> "COMPLETADA".equalsIgnoreCase(c.getEstado())).count()));
        return r;
    }

    private ReporteDto comprasProveedor(LocalDateTime desde, LocalDateTime hasta) {
        Map<String, ProveedorAcumulado> acumulado = new LinkedHashMap<>();
        for (CompraDto c : compraService.findAllConDetalles()) {
            if (!enRango(c.getFecha(), desde, hasta) || "ANULADA".equalsIgnoreCase(c.getEstado())) continue;
            String proveedor = s(c.getProveedorNombre());
            ProveedorAcumulado a = acumulado.computeIfAbsent(proveedor, k -> new ProveedorAcumulado());
            a.nombre = proveedor;
            a.facturas++;
            a.total = a.total.add(nz(c.getMontoTotal()));
            for (CompraDetalleDto d : c.getDetalles()) {
                a.unidades += d.getCantidad() == null ? 0 : d.getCantidad();
            }
        }
        ReporteDto r = base("compras-proveedor", "Compras por proveedor", "Ranking de proveedores del período");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Proveedor", "Facturas", "Unidades", "Total comprado"));
        for (ProveedorAcumulado a : acumulado.values().stream()
                .sorted((x, y) -> y.total.compareTo(x.total)).toList()) {
            r.fila(a.nombre, String.valueOf(a.facturas), String.valueOf(a.unidades), money(a.total));
        }
        r.kpi("Proveedores", String.valueOf(acumulado.size()));
        r.kpi("Facturas", String.valueOf(acumulado.values().stream().mapToLong(a -> a.facturas).sum()));
        r.kpi("Total comprado", money(acumulado.values().stream().map(a -> a.total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        return r;
    }

    private ReporteDto inventario() {
        List<ProductoDto> productos = productoService.findAll();
        BigDecimal valorCosto = BigDecimal.ZERO;
        BigDecimal valorVenta = BigDecimal.ZERO;
        long bajoStock = 0;

        ReporteDto r = base("inventario", "Inventario valorizado", "Stock actual, costo y valor de venta por producto");
        r.setColumnas(List.of("Producto", "SKU", "Categoría", "Stock", "Costo", "Valor costo",
                "Precio venta", "Valor venta", "Estado"));
        for (ProductoDto p : productos) {
            int stock = p.getStock() == null ? 0 : p.getStock();
            BigDecimal costo = nz(p.getCostoActual());
            BigDecimal venta = nz(p.getPrecioVenta());
            BigDecimal valorC = costo.multiply(BigDecimal.valueOf(stock));
            BigDecimal valorV = venta.multiply(BigDecimal.valueOf(stock));
            valorCosto = valorCosto.add(valorC);
            valorVenta = valorVenta.add(valorV);
            boolean bajo = stock <= (p.getStockMinimo() == null ? 5 : p.getStockMinimo());
            if (bajo) bajoStock++;
            r.fila(s(p.getNombre()), s(p.getSku()), s(p.getCategoria()), String.valueOf(stock),
                    money(costo), money(valorC), money(venta), money(valorV), bajo ? "Bajo stock" : "OK");
        }
        r.kpi("Productos", String.valueOf(productos.size()));
        r.kpi("Valor a costo", money(valorCosto));
        r.kpi("Valor a venta", money(valorVenta));
        r.kpi("Bajo stock", String.valueOf(bajoStock));
        return r;
    }

    private ReporteDto movimientos(LocalDateTime desde, LocalDateTime hasta) {
        List<MovimientoStockDto> movimientos = movimientoStockService.findAll().stream()
                .filter(m -> enRango(m.getCreadoEn(), desde, hasta))
                .toList();
        long entradas = 0;
        long salidas = 0;
        for (MovimientoStockDto m : movimientos) {
            int cantidad = m.getCantidad() == null ? 0 : m.getCantidad();
            if (cantidad > 0) entradas += cantidad;
            if (cantidad < 0) salidas += Math.abs(cantidad);
        }

        ReporteDto r = base("movimientos", "Movimientos de stock", "Entradas y salidas del período");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Fecha", "Producto", "Tipo", "Cantidad", "Stock", "Costo", "Motivo"));
        for (MovimientoStockDto m : movimientos) {
            int cantidad = m.getCantidad() == null ? 0 : m.getCantidad();
            r.fila(fechaHora(m.getCreadoEn()), s(m.getProductoNombre()), s(m.getTipo()),
                    (cantidad > 0 ? "+" : "") + cantidad,
                    m.getStockAnterior() + " → " + m.getStockNuevo(),
                    money(nz(m.getCostoUnitario())), s(m.getMotivo()));
        }
        r.kpi("Movimientos", String.valueOf(movimientos.size()));
        r.kpi("Entradas", String.valueOf(entradas));
        r.kpi("Salidas", String.valueOf(salidas));
        return r;
    }

    private ReporteDto anulaciones(LocalDateTime desde, LocalDateTime hasta) {
        List<String[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (VentaDto v : ventaService.findAll()) {
            if (!"ANULADA".equalsIgnoreCase(v.getEstado()) || !enRango(v.getFecha(), desde, hasta)) continue;
            total = total.add(nz(v.getMontoTotal()));
            filas.add(new String[]{s(v.getNumeroFactura()), "VENTA", fecha(v.getFecha()),
                    s(v.getClienteNombre()), money(v.getMontoTotal()), fechaHora(v.getFechaAnulacion())});
        }
        for (CompraDto c : compraService.findAll()) {
            if (!"ANULADA".equalsIgnoreCase(c.getEstado()) || !enRango(c.getFecha(), desde, hasta)) continue;
            total = total.add(nz(c.getMontoTotal()));
            filas.add(new String[]{s(c.getNumeroFactura()), "COMPRA", fecha(c.getFecha()),
                    s(c.getProveedorNombre()), money(c.getMontoTotal()), fechaHora(c.getFechaAnulacion())});
        }

        ReporteDto r = base("anulaciones", "Anulaciones", "Ventas y compras anuladas en el período");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Documento", "Tipo", "Fecha", "Cliente / Proveedor", "Total", "Anulada el"));
        for (String[] fila : filas) {
            r.fila(fila);
        }
        r.kpi("Anulaciones", String.valueOf(filas.size()));
        r.kpi("Monto anulado", money(total));
        return r;
    }

    private ReporteDto sinMovimiento(LocalDateTime desde, LocalDateTime hasta) {
        List<MovimientoStockDto> todos = movimientoStockService.findAll();
        Set<Long> conMovimiento = todos.stream()
                .filter(m -> enRango(m.getCreadoEn(), desde, hasta))
                .map(MovimientoStockDto::getProductoId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));

        Map<Long, LocalDateTime> ultimaActividad = new HashMap<>();
        for (MovimientoStockDto m : todos) {
            if (m.getProductoId() == null || m.getCreadoEn() == null) continue;
            LocalDateTime actual = ultimaActividad.get(m.getProductoId());
            if (actual == null || m.getCreadoEn().isAfter(actual)) {
                ultimaActividad.put(m.getProductoId(), m.getCreadoEn());
            }
        }

        BigDecimal valor = BigDecimal.ZERO;
        long cantidad = 0;
        ReporteDto r = base("sin-movimiento", "Productos sin movimiento", "Productos activos sin movimientos en el rango");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Producto", "SKU", "Stock", "Costo", "Valor costo", "Última actividad"));
        for (ProductoDto p : productoService.findAll()) {
            if (p.getId() == null || conMovimiento.contains(p.getId())) continue;
            cantidad++;
            int stock = p.getStock() == null ? 0 : p.getStock();
            BigDecimal costo = nz(p.getCostoActual());
            BigDecimal valorC = costo.multiply(BigDecimal.valueOf(stock));
            valor = valor.add(valorC);
            LocalDateTime ultima = ultimaActividad.get(p.getId());
            r.fila(s(p.getNombre()), s(p.getSku()), String.valueOf(stock), money(costo),
                    money(valorC), ultima == null ? "Sin movimientos" : fecha(ultima));
        }
        r.kpi("Productos", String.valueOf(cantidad));
        r.kpi("Valor inmovilizado", money(valor));
        return r;
    }

    private ReporteDto margen(LocalDateTime desde, LocalDateTime hasta) {
        Map<Long, ProductoDto> productos = productosPorId();
        Map<Long, Acumulado> acumulado = acumularProductos(
                ventaService.findAllConDetalles().stream()
                        .filter(v -> !"ANULADA".equalsIgnoreCase(v.getEstado()))
                        .filter(v -> enRango(v.getFecha(), desde, hasta))
                        .toList(), productos);

        ReporteDto r = base("margen", "Margen estimado", "Ingresos menos costo actual de los productos vendidos");
        r.setMostrarFechas(true);
        r.setColumnas(List.of("Producto", "Ingresos", "Costo", "Margen", "Margen %"));
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal costo = BigDecimal.ZERO;
        for (Acumulado a : ordenarPorIngreso(acumulado)) {
            ingresos = ingresos.add(a.ingresos);
            costo = costo.add(a.costo);
            BigDecimal margen = a.ingresos.subtract(a.costo);
            r.fila(a.nombre, money(a.ingresos), money(a.costo), money(margen),
                    a.ingresos.signum() == 0 ? "0 %" : margen.multiply(BigDecimal.valueOf(100))
                            .divide(a.ingresos, 1, RoundingMode.HALF_UP) + " %");
        }
        BigDecimal margen = ingresos.subtract(costo);
        r.kpi("Ingresos", money(ingresos));
        r.kpi("Costo estimado", money(costo));
        r.kpi("Margen", money(margen));
        r.kpi("Margen %", ingresos.signum() == 0 ? "0 %" : margen.multiply(BigDecimal.valueOf(100))
                .divide(ingresos, 1, RoundingMode.HALF_UP) + " %");
        return r;
    }

    private Map<Long, Acumulado> acumularProductos(List<VentaDto> ventas, Map<Long, ProductoDto> productos) {
        Map<Long, Acumulado> acumulado = new LinkedHashMap<>();
        for (VentaDto v : ventas) {
            for (VentaDetalleDto d : v.getDetalles()) {
                if (d.getProductoId() == null) continue;
                Acumulado a = acumulado.computeIfAbsent(d.getProductoId(), id -> new Acumulado());
                ProductoDto p = productos.get(d.getProductoId());
                a.nombre = p != null ? s(p.getNombre()) : s(d.getProductoNombre());
                int cantidad = d.getCantidad() == null ? 0 : d.getCantidad();
                a.unidades += cantidad;
                a.ingresos = a.ingresos.add(nz(d.getSubtotal()));
                BigDecimal costoUnitario = p == null ? BigDecimal.ZERO : nz(p.getCostoActual());
                a.costo = a.costo.add(costoUnitario.multiply(BigDecimal.valueOf(cantidad)));
            }
        }
        return acumulado;
    }

    private List<Acumulado> ordenarPorIngreso(Map<Long, Acumulado> acumulado) {
        return acumulado.values().stream()
                .sorted((a, b) -> b.ingresos.compareTo(a.ingresos))
                .toList();
    }

    private Map<Long, ProductoDto> productosPorId() {
        return productoService.findAll().stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(ProductoDto::getId, p -> p, (a, b) -> a));
    }

    private ReporteDto base(String tipo, String titulo, String subtitulo) {
        ReporteDto r = new ReporteDto();
        r.setTipo(tipo);
        r.setTitulo(titulo);
        r.setSubtitulo(subtitulo);
        return r;
    }

    private boolean enRango(LocalDateTime fecha, LocalDateTime desde, LocalDateTime hasta) {
        return fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private String money(BigDecimal valor) {
        return FormatoNumero.miles(nz(valor));
    }

    private BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private BigDecimal promedio(BigDecimal total, long cantidad) {
        return cantidad == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(cantidad), 0, RoundingMode.HALF_UP);
    }

    private String fecha(LocalDateTime valor) {
        return valor == null ? "-" : valor.format(FECHA);
    }

    private String fechaHora(LocalDateTime valor) {
        return valor == null ? "-" : valor.format(FECHA_HORA);
    }

    private String s(String valor) {
        return valor == null ? "" : valor;
    }

    private static class Acumulado {
        private String nombre = "";
        private long unidades;
        private BigDecimal ingresos = BigDecimal.ZERO;
        private BigDecimal costo = BigDecimal.ZERO;
    }

    private static class ClienteAcumulado {
        private String nombre = "";
        private long facturas;
        private BigDecimal total = BigDecimal.ZERO;
        private LocalDateTime ultima;
    }

    private static class ProveedorAcumulado {
        private String nombre = "";
        private long facturas;
        private long unidades;
        private BigDecimal total = BigDecimal.ZERO;
    }
}
