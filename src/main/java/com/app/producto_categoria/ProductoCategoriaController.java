package com.app.producto_categoria;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Controller("/api/producto-categorias")
public class ProductoCategoriaController {

    private final ProductoCategoriaService productoCategoriaService;

    public ProductoCategoriaController(ProductoCategoriaService productoCategoriaService) {
        this.productoCategoriaService = productoCategoriaService;
    }

    @Get
    public HttpResponse<List<ProductoCategoriaDto>> getAll() {
        return HttpResponse.ok(productoCategoriaService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<ProductoCategoriaDto> getById(@PathVariable Long id) {
        return productoCategoriaService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<ProductoCategoriaDto> create(@Valid @Body ProductoCategoriaDto dto) {
        return HttpResponse.created(productoCategoriaService.save(dto));
    }

    @Put("/{id}")
    public HttpResponse<ProductoCategoriaDto> update(@PathVariable Long id, @Valid @Body ProductoCategoriaDto dto) {
        return productoCategoriaService.update(id, dto)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Delete("/{id}")
    public HttpResponse<?> delete(@PathVariable Long id) {
        if (productoCategoriaService.delete(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
