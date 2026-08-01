package com.app.compra.controller;

import com.app.compra.dto.CompraDetalleDto;
import com.app.compra.dto.CompraDto;
import com.app.compra.service.CompraService;
import com.app.producto.service.ProductoService;
import com.app.proveedor.ProveedorService;
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

@Controller("/ui/compras")
public class CompraUiController {

    private static final Logger LOG = LoggerFactory.getLogger(CompraUiController.class);

    private final CompraService compraService;
    private final ProveedorService proveedorService;
    private final ProductoService productoService;

    public CompraUiController(CompraService compraService,
                              ProveedorService proveedorService,
                              ProductoService productoService) {
        this.compraService = compraService;
        this.proveedorService = proveedorService;
        this.productoService = productoService;
    }

    @View("compra/table")
    @Get("/list{?estado}")
    public Map<String, Object> list(@Nullable String estado) {
        List<CompraDto> compras = compraService.findAll();
        if (estado != null && !estado.trim().isEmpty() && !"TODAS".equalsIgnoreCase(estado)) {
            compras = compras.stream()
                    .filter(c -> estado.equalsIgnoreCase(c.getEstado()))
                    .collect(Collectors.toList());
        }
        return Map.of("compras", compras, "estadoFiltro", estado != null ? estado.toUpperCase() : "TODAS");
    }

    @View("compra/form")
    @Get("/form")
    public Map<String, Object> form() {
        return Map.of("proveedores", proveedorService.findAll(), "productos", productoService.findAll());
    }

    @View("compra/producto_results")
    @Get("/buscar-producto{?q}")
    public Map<String, Object> buscarProducto(@Nullable String q) {
        return Map.of("productos", productoService.buscar(q));
    }

    @View("compra/detalle")
    @Get("/detalle{?id}")
    public Map<String, Object> detalle(@Nullable Long id) {
        if (id != null) {
            return compraService.findById(id)
                    .map(compra -> Map.<String, Object>of("compra", compra))
                    .orElseGet(Map::of);
        }
        return Map.of();
    }

    @View("compra/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Nullable @Body Map<String, Object> body, HttpRequest<?> request) {

        if (body == null || body.isEmpty()) {
            LOG.warn("Cuerpo de la petición de compra recibido es NULO o VACÍO.");
            return Map.of("compras", compraService.findAll(), "proveedores", proveedorService.findAll(), "productos", productoService.findAll(), "error", "No se recibieron datos de la compra.");
        }

        CompraDto compraDto = new CompraDto();

        Object proveedorIdObj = body.get("proveedorId");

        if (proveedorIdObj != null && !proveedorIdObj.toString().trim().isEmpty()) {
            try {
                compraDto.setProveedorId(Long.parseLong(proveedorIdObj.toString().trim()));
            } catch (NumberFormatException e) {
                LOG.error("Error al parsear proveedorId: {}", proveedorIdObj, e);
                return Map.of("compras", compraService.findAll(), "proveedores", proveedorService.findAll(), "productos", productoService.findAll(), "error", "Identificador de proveedor no válido.");
            }
        } else {
            LOG.warn("Falta el proveedorId en la petición.");
            return Map.of("compras", compraService.findAll(), "proveedores", proveedorService.findAll(), "productos", productoService.findAll(), "error", "Debe seleccionar un proveedor válido.");
        }

        Object condicionObj = body.get("condicion");
        String condicion = condicionObj != null ? condicionObj.toString().trim() : "Contado";
        compraDto.setCondicion(condicion);

        List<String> prodIds = getAsList(body.get("productoId"));
        List<String> cantidades = getAsList(body.get("cantidad"));
        List<String> precios = getAsList(body.get("precioCosto"));

        List<CompraDetalleDto> detalles = new ArrayList<>();
        for (int i = 0; i < prodIds.size(); i++) {
            String pIdStr = prodIds.get(i);
            if (pIdStr == null || pIdStr.trim().isEmpty()) continue;

            CompraDetalleDto d = new CompraDetalleDto();
            d.setProductoId(Long.parseLong(pIdStr.trim()));

            int qty = 1;
            if (i < cantidades.size() && cantidades.get(i) != null && !cantidades.get(i).trim().isEmpty()) {
                qty = Integer.parseInt(cantidades.get(i).trim());
            }
            d.setCantidad(qty);

            if (i < precios.size() && precios.get(i) != null && !precios.get(i).trim().isEmpty()) {
                d.setPrecioCosto(new BigDecimal(precios.get(i).trim()));
            }

            detalles.add(d);
        }

        compraDto.setDetalles(detalles);

        try {
            CompraDto compraGuardada = compraService.registrarCompra(compraDto);
            return Map.of("compras", compraService.findAll(), "proveedores", proveedorService.findAll(), "productos", productoService.findAll(), "mensaje", "Compra factura " + compraGuardada.getNumeroFactura() + " registrada con éxito.");
        } catch (Exception e) {
            LOG.error("Error al registrar la compra en el servicio: {}", e.getMessage(), e);
            return Map.of("compras", compraService.findAll(), "proveedores", proveedorService.findAll(), "productos", productoService.findAll(), "error", e.getMessage() != null ? e.getMessage() : "Error al registrar la compra.");
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

    @View("compra/table")
    @Post(uri = "/anular/{id}", consumes = MediaType.ALL)
    public Map<String, Object> anular(@PathVariable Long id) {
        boolean anulado = compraService.anular(id);
        if (anulado) {
            return Map.of("compras", compraService.findAll(), "proveedores", proveedorService.findAll(), "productos", productoService.findAll(), "mensaje", "Compra anulada correctamente.");
        } else {
            return Map.of("compras", compraService.findAll(), "proveedores", proveedorService.findAll(), "productos", productoService.findAll(), "error", "No se pudo anular la compra.");
        }
    }
}
