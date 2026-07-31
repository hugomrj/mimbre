package com.app.producto.controller;

import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.service.ProductoService;
import com.app.producto_categoria.ProductoCategoriaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import jakarta.validation.Valid;

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
    @Get("/list")
    public Map<String, Object> list() {
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
    public Map<String, Object> save(@Valid @Body ProductoFormDto formDto) {
        if (formDto.getId() != null) {
            productoService.update(formDto.getId(), formDto);
            return Map.of("productos", productoService.findAll(), "mensaje", "Producto actualizado correctamente.");
        } else {
            productoService.save(formDto);
            return Map.of("productos", productoService.findAll(), "mensaje", "Producto creado correctamente.");
        }
    }

    @View("producto/table")
    @Post(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deletePost(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("producto/table")
    @Delete(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteDelete(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("producto/table")
    @Delete(uri = "/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteById(@PathVariable Long id) {
        return performDelete(id);
    }

    private Map<String, Object> performDelete(Long id) {
        try {
            boolean eliminado = productoService.delete(id);
            if (eliminado) {
                return Map.of("productos", productoService.findAll(), "mensaje", "Producto eliminado correctamente.");
            } else {
                return Map.of("productos", productoService.findAll(), "error", "No se encontró el producto a eliminar.");
            }
        } catch (Exception e) {
            return Map.of("productos", productoService.findAll(), "error", e.getMessage() != null ? e.getMessage() : "No se puede eliminar el producto porque está asociado a ventas u otros registros.");
        }
    }
}
