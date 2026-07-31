package com.app.venta.controller;

import com.app.venta.dto.VentaDto;
import com.app.venta.service.VentaService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @Get
    public HttpResponse<List<VentaDto>> getAll() {
        return HttpResponse.ok(ventaService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<VentaDto> getById(@PathVariable Long id) {
        return ventaService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<VentaDto> create(@Body VentaDto ventaDto) {
        return HttpResponse.created(ventaService.registrarVenta(ventaDto));
    }

    @Post("/{id}/anular")
    public HttpResponse<?> anular(@PathVariable Long id) {
        if (ventaService.anular(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
