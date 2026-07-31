package com.app.producto.controller;

import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.service.ProductoService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Controller("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Get
    public HttpResponse<List<ProductoDto>> getAll() {
        return HttpResponse.ok(productoService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<ProductoDto> getById(@PathVariable Long id) {
        return productoService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<ProductoDto> create(@Valid @Body ProductoFormDto formDto) {
        return HttpResponse.created(productoService.save(formDto));
    }

    @Put("/{id}")
    public HttpResponse<ProductoDto> update(@PathVariable Long id, @Valid @Body ProductoFormDto formDto) {
        return productoService.update(id, formDto)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Delete("/{id}")
    public HttpResponse<?> delete(@PathVariable Long id) {
        if (productoService.delete(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
