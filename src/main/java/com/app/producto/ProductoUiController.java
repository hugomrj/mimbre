package com.app.producto;

import com.app.producto_categoria.ProductoCategoriaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.HashMap;
import java.util.Map;

@Controller("/ui/productos")
public class ProductoUiController {

    private final ProductoService productoService;
    private final ProductoCategoriaService productoCategoriaService;

    public ProductoUiController(ProductoService productoService, ProductoCategoriaService productoCategoriaService) {
        this.productoService = productoService;
        this.productoCategoriaService = productoCategoriaService;
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
        model.put("categorias", productoCategoriaService.findAll());
        if (id != null) {
            productoService.findById(id).ifPresent(producto -> model.put("producto", producto));
        }
        return model;
    }

    @View("producto/results")
    @Get("/buscar-producto{?q}")
    public Map<String, Object> buscarProducto(@Nullable String q) {
        return Map.of("productos", productoService.buscar(q));
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
