package com.app.venta.controller;

import com.app.cliente.ClienteService;
import com.app.producto.service.ProductoService;
import com.app.venta.dto.VentaDetalleDto;
import com.app.venta.dto.VentaDto;
import com.app.venta.service.VentaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/ui/ventas")
public class VentaUiController {

    private static final Logger LOG = LoggerFactory.getLogger(VentaUiController.class);

    private final VentaService ventaService;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    public VentaUiController(VentaService ventaService, ClienteService clienteService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    @View("venta/table")
    @Get("/list{?estado}")
    public Map<String, Object> list(@Nullable String estado) {
        List<VentaDto> ventas = ventaService.findAll();
        if (estado != null && !estado.trim().isEmpty() && !"TODAS".equalsIgnoreCase(estado)) {
            ventas = ventas.stream()
                    .filter(v -> estado.equalsIgnoreCase(v.getEstado()))
                    .collect(Collectors.toList());
        }
        return Map.of("ventas", ventas, "estadoFiltro", estado != null ? estado.toUpperCase() : "TODAS");
    }

    @View("venta/form")
    @Get("/form")
    public Map<String, Object> form() {
        return Map.of();
    }

    @View("venta/detalle")
    @Get("/detalle{?id}")
    public Map<String, Object> detalle(@Nullable Long id) {
        if (id != null) {
            return ventaService.findById(id)
                    .map(venta -> Map.<String, Object>of("venta", venta))
                    .orElseGet(Map::of);
        }
        return Map.of();
    }

    @View("venta/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Nullable @Body Map<String, Object> body, HttpRequest<?> request) {
        LOG.info("=========================================");
        LOG.info("=== RECIBIDA PETICIÓN DE GUARDAR VENTA ===");
        LOG.info("=========================================");

        if (body == null || body.isEmpty()) {
            LOG.warn("Cuerpo de la petición recibido es NULO o VACÍO.");
            return Map.of("ventas", ventaService.findAll(), "error", "No se recibieron datos de la venta.");
        }

        LOG.info("Cuerpo del formulario parseado como Map: {}", body);

        VentaDto ventaDto = new VentaDto();
        
        Object clienteIdObj = body.get("clienteId");
        LOG.info("clienteId objeto recibido: {}", clienteIdObj);

        if (clienteIdObj != null && !clienteIdObj.toString().trim().isEmpty()) {
            try {
                ventaDto.setClienteId(Long.parseLong(clienteIdObj.toString().trim()));
            } catch (NumberFormatException e) {
                LOG.error("Error al parsear clienteId: {}", clienteIdObj, e);
                return Map.of("ventas", ventaService.findAll(), "error", "Identificador de cliente no válido.");
            }
        } else {
            LOG.warn("Falta el clienteId en la petición. No se guardará la venta.");
            return Map.of("ventas", ventaService.findAll(), "error", "Debe seleccionar un cliente válido.");
        }
        
        Object condicionObj = body.get("condicion");
        String condicion = condicionObj != null ? condicionObj.toString().trim() : "Contado";
        ventaDto.setCondicion(condicion);
        LOG.info("Condición de venta establecida: '{}'", condicion);

        List<String> prodIds = getAsList(body.get("productoId"));
        List<String> cantidades = getAsList(body.get("cantidad"));
        List<String> precios = getAsList(body.get("precioUnitario"));

        LOG.info("Productos IDs recibidos: {}", prodIds);
        LOG.info("Cantidades recibidas: {}", cantidades);
        LOG.info("Precios recibidos: {}", precios);

        List<VentaDetalleDto> detalles = new ArrayList<>();
        for (int i = 0; i < prodIds.size(); i++) {
            String pIdStr = prodIds.get(i);
            if (pIdStr == null || pIdStr.trim().isEmpty()) continue;
            
            VentaDetalleDto d = new VentaDetalleDto();
            d.setProductoId(Long.parseLong(pIdStr.trim()));
            
            int qty = 1;
            if (i < cantidades.size() && cantidades.get(i) != null && !cantidades.get(i).trim().isEmpty()) {
                qty = Integer.parseInt(cantidades.get(i).trim());
            }
            d.setCantidad(qty);

            if (i < precios.size() && precios.get(i) != null && !precios.get(i).trim().isEmpty()) {
                d.setPrecioUnitario(new BigDecimal(precios.get(i).trim()));
            }

            detalles.add(d);
            LOG.info("Detalle agregado #{} -> Producto ID: {}, Cantidad: {}, Precio: {}", 
                    i + 1, d.getProductoId(), d.getCantidad(), d.getPrecioUnitario());
        }

        ventaDto.setDetalles(detalles);

        try {
            LOG.info("Registrando venta en VentaService...");
            VentaDto ventaGuardada = ventaService.registrarVenta(ventaDto);
            LOG.info("¡Venta registrada con éxito! ID: {}, Nro Factura: {}, Total: {}", 
                    ventaGuardada.getId(), ventaGuardada.getNumeroFactura(), ventaGuardada.getMontoTotal());
            return Map.of("ventas", ventaService.findAll(), "mensaje", "Venta Factura " + ventaGuardada.getNumeroFactura() + " registrada con éxito.");
        } catch (Exception e) {
            LOG.error("Error al registrar la venta en el servicio: {}", e.getMessage(), e);
            return Map.of("ventas", ventaService.findAll(), "error", e.getMessage() != null ? e.getMessage() : "Error al registrar la venta.");
        }
    }

    private List<String> getAsList(Object val) {
        if (val == null) {
            return new ArrayList<>();
        }
        if (val instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item != null) result.add(item.toString());
            }
            return result;
        }
        if (val instanceof Object[] arr) {
            List<String> result = new ArrayList<>();
            for (Object item : arr) {
                if (item != null) result.add(item.toString());
            }
            return result;
        }
        return new ArrayList<>(List.of(val.toString()));
    }

    @View("venta/table")
    @Post(uri = "/anular/{id}", consumes = MediaType.ALL)
    public Map<String, Object> anular(@PathVariable Long id) {
        LOG.info("Anulando venta con ID: {}", id);
        boolean anulado = ventaService.anular(id);
        if (anulado) {
            return Map.of("ventas", ventaService.findAll(), "mensaje", "Venta anulada correctamente.");
        } else {
            return Map.of("ventas", ventaService.findAll(), "error", "No se pudo anular la venta.");
        }
    }
}
