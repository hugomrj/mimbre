package com.app.producto;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
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
    public HttpResponse<ProductoDto> create(@Body ProductoDto productoDto) {
        return HttpResponse.created(productoService.save(productoDto));
    }

    @Put("/{id}")
    public HttpResponse<ProductoDto> update(@PathVariable Long id, @Body ProductoDto productoDto) {
        return productoService.update(id, productoDto)
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
