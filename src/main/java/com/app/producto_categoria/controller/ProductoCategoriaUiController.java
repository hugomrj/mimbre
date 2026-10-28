package com.app.producto_categoria.controller;

import com.app.producto_categoria.dto.ProductoCategoriaDto;
import com.app.producto_categoria.service.ProductoCategoriaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@Controller("/ui/categorias")
public class ProductoCategoriaUiController {

    private final ProductoCategoriaService productoCategoriaService;

    public ProductoCategoriaUiController(ProductoCategoriaService productoCategoriaService) {
        this.productoCategoriaService = productoCategoriaService;
    }

    @View("producto_categoria/table")
    @Get("/list{?page,size}")
    public Map<String, Object> list(@Nullable @QueryValue("page") Integer page,
                                    @Nullable @QueryValue("size") Integer size) {
        return buildTableModel(page, size, null, null);
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
        String mensaje;
        if (dto.getId() != null) {
            productoCategoriaService.update(dto.getId(), dto);
            mensaje = "Categoría actualizada correctamente.";
        } else {
            productoCategoriaService.save(dto);
            mensaje = "Categoría registrada correctamente.";
        }
        return buildTableModel(0, 10, mensaje, null);
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
                return buildTableModel(0, 10, "Categoría eliminada correctamente.", null);
            } else {
                return buildTableModel(0, 10, null, "No se encontró la categoría a eliminar.");
            }
        } catch (Exception e) {
            return buildTableModel(0, 10, null,
                    e.getMessage() != null ? e.getMessage() : "No se puede eliminar la categoría porque está asociada a productos u otros registros.");
        }
    }

    private Map<String, Object> buildTableModel(Integer page, Integer size, String mensaje, String error) {
        int pageNum = (page != null && page >= 0) ? page : 0;
        int sizeNum = (size != null && size > 0) ? size : 10;

        Page<ProductoCategoriaDto> pagina = productoCategoriaService.findPage(pageNum, sizeNum);
        long totalElements = pagina.getTotalSize();
        int startItem = totalElements == 0 ? 0 : (pagina.getPageNumber() * pagina.getSize() + 1);
        int endItem = (int) Math.min((long) (pagina.getPageNumber() + 1) * pagina.getSize(), totalElements);

        Map<String, Object> model = new HashMap<>();
        model.put("categorias", pagina.getContent());
        model.put("currentPage", pagina.getPageNumber());
        model.put("totalPages", pagina.getTotalPages());
        model.put("totalElements", totalElements);
        model.put("size", pagina.getSize());
        model.put("startItem", startItem);
        model.put("endItem", endItem);
        model.put("baseUrl", "/ui/categorias/list");

        if (mensaje != null) model.put("mensaje", mensaje);
        if (error != null) model.put("error", error);

        return model;
    }
}