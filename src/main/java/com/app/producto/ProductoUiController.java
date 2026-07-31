package com.app.producto;

import com.app.category.CategoryService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller("/ui/productos")
public class ProductoUiController {

    private final ProductoService productoService;
    private final CategoryService categoryService;

    public ProductoUiController(ProductoService productoService, CategoryService categoryService) {
        this.productoService = productoService;
        this.categoryService = categoryService;
    }

    @View("producto/table")
    @Get("/table")
    public Map<String, Object> table() {
        return Map.of("productos", productoService.findAll());
    }

    @View("producto/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("categorias", categoryService.findAll());
        if (id != null) {
            productoService.findById(id).ifPresent(producto -> model.put("producto", producto));
        }
        return model;
    }

    @View("producto/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Body ProductoDto productoDto) {
        if (productoDto.getId() != null) {
            productoService.update(productoDto.getId(), productoDto);
        } else {
            productoService.save(productoDto);
        }
        return Map.of("productos", productoService.findAll());
    }
}
