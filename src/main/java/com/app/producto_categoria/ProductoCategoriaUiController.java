package com.app.producto_categoria;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import jakarta.validation.Valid;

import java.util.Map;

@Controller("/ui/categorias")
public class ProductoCategoriaUiController {

    private final ProductoCategoriaService productoCategoriaService;

    public ProductoCategoriaUiController(ProductoCategoriaService productoCategoriaService) {
        this.productoCategoriaService = productoCategoriaService;
    }

    @View("producto_categoria/table")
    @Get("/list")
    public Map<String, Object> list() {
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
    public Map<String, Object> save(@Valid @Body ProductoCategoriaDto dto) {
        if (dto.getId() != null) {
            productoCategoriaService.update(dto.getId(), dto);
            return Map.of("categorias", productoCategoriaService.findAll(), "mensaje", "Categoría actualizada correctamente.");
        } else {
            productoCategoriaService.save(dto);
            return Map.of("categorias", productoCategoriaService.findAll(), "mensaje", "Categoría registrada correctamente.");
        }
    }

    @View("producto_categoria/table")
    @Post(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deletePost(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("producto_categoria/table")
    @Delete(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteDelete(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("producto_categoria/table")
    @Delete(uri = "/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteById(@PathVariable Long id) {
        return performDelete(id);
    }

    private Map<String, Object> performDelete(Long id) {
        try {
            boolean eliminado = productoCategoriaService.delete(id);
            if (eliminado) {
                return Map.of("categorias", productoCategoriaService.findAll(), "mensaje", "Categoría eliminada correctamente.");
            } else {
                return Map.of("categorias", productoCategoriaService.findAll(), "error", "No se encontró la categoría a eliminar.");
            }
        } catch (Exception e) {
            return Map.of("categorias", productoCategoriaService.findAll(), "error", e.getMessage() != null ? e.getMessage() : "No se puede eliminar la categoría porque está asociada a productos u otros registros.");
        }
    }
}
