package com.app.compra.controller;

import com.app.compra.dto.CompraDto;
import com.app.compra.service.CompraService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Controller("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @Get
    public HttpResponse<List<CompraDto>> getAll() {
        return HttpResponse.ok(compraService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<CompraDto> getById(@PathVariable Long id) {
        return compraService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<CompraDto> create(@Valid @Body CompraDto compraDto) {
        return HttpResponse.created(compraService.registrarCompra(compraDto));
    }

    @Post("/{id}/anular")
    public HttpResponse<?> anular(@PathVariable Long id) {
        if (compraService.anular(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
