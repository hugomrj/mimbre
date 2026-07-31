package com.app.producto_categoria;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.Map;

@Controller("/ui/categorias")
public class ProductoCategoriaUiController {

    private final ProductoCategoriaService productoCategoriaService;

    public ProductoCategoriaUiController(ProductoCategoriaService productoCategoriaService) {
        this.productoCategoriaService = productoCategoriaService;
    }

    @View("producto_categoria/table")
    @Get("/table")
    public Map<String, Object> table() {
        return Map.of("categorias", productoCategoriaService.findAll());
    }

    @View("producto_categoria/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        if (id != null) {
            return productoCategoriaService.findById(id)
                    .map(categoria -> (Map<String, Object>) Map.<String, Object>of("categoria", categoria))
                    .orElseGet(Map::of);
        }
        return Map.of();
    }

    @View("producto_categoria/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Body ProductoCategoriaDto dto) {
        if (dto.getId() != null) {
            productoCategoriaService.update(dto.getId(), dto);
        } else {
            productoCategoriaService.save(dto);
        }
        return Map.of("categorias", productoCategoriaService.findAll());
    }
}
