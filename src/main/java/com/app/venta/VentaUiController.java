package com.app.venta;

import com.app.cliente.ClienteService;
import com.app.producto.ProductoService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller("/ui/ventas")
public class VentaUiController {

    private final VentaService ventaService;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    public VentaUiController(VentaService ventaService, ClienteService clienteService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    @View("venta/table")
    @Get("/table")
    public Map<String, Object> table() {
        return Map.of("ventas", ventaService.findAll());
    }

    @View("venta/form")
    @Get("/form")
    public Map<String, Object> form() {
        return Map.of(
                "clientes", clienteService.findAll(),
                "productos", productoService.findAll()
        );
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
    public Map<String, Object> save(HttpRequest<?> request) {
        var parameters = request.getParameters();

        VentaDto ventaDto = new VentaDto();
        
        String clienteIdStr = parameters.get("clienteId");
        if (clienteIdStr != null && !clienteIdStr.isEmpty()) {
            ventaDto.setClienteId(Long.parseLong(clienteIdStr));
        }
        
        ventaDto.setCondicion(parameters.get("condicion", String.class).orElse("Contado"));

        List<String> prodIds = parameters.getAll("productoId");
        List<String> cantidades = parameters.getAll("cantidad");
        List<String> precios = parameters.getAll("precioUnitario");

        List<VentaDetalleDto> detalles = new ArrayList<>();
        if (prodIds != null) {
            for (int i = 0; i < prodIds.size(); i++) {
                if (prodIds.get(i) == null || prodIds.get(i).isEmpty()) continue;
                
                VentaDetalleDto d = new VentaDetalleDto();
                d.setProductoId(Long.parseLong(prodIds.get(i)));
                
                int qty = 1;
                if (cantidades != null && i < cantidades.size() && !cantidades.get(i).isEmpty()) {
                    qty = Integer.parseInt(cantidades.get(i));
                }
                d.setCantidad(qty);

                if (precios != null && i < precios.size() && !precios.get(i).isEmpty()) {
                    d.setPrecioUnitario(new BigDecimal(precios.get(i)));
                }

                detalles.add(d);
            }
        }

        ventaDto.setDetalles(detalles);
        ventaService.registrarVenta(ventaDto);

        return Map.of("ventas", ventaService.findAll());
    }

    @View("venta/table")
    @Post(uri = "/anular/{id}")
    public Map<String, Object> anular(@PathVariable Long id) {
        ventaService.anular(id);
        return Map.of("ventas", ventaService.findAll());
    }
}
