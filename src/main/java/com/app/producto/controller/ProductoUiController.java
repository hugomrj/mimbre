package com.app.producto.controller;

import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.service.ProductoService;
import com.app.producto_categoria.service.ProductoCategoriaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
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
    @Get("/list{?page,size}")
    public Map<String, Object> list(@Nullable @QueryValue("page") Integer page,
                                    @Nullable @QueryValue("size") Integer size) {
        return buildTableModel(page, size, null, null);
    }

    @View("producto/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("categorias", productoCategoriaService.findAll());
        if (id != null) {
            productoService.findById(id).ifPresent(p -> model.put("producto", p));
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
        String mensaje;
        if (formDto.getId() != null) {
            productoService.update(formDto.getId(), formDto);
            mensaje = "Producto actualizado correctamente.";
        } else {
            productoService.save(formDto);
            mensaje = "Producto creado correctamente.";
        }
        return buildTableModel(0, 10, mensaje, null);
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
            return eliminado
                    ? buildTableModel(0, 10, "Producto eliminado correctamente.", null)
                    : buildTableModel(0, 10, null, "No se encontró el producto a eliminar.");
        } catch (Exception e) {
            return buildTableModel(0, 10, null,
                    e.getMessage() != null ? e.getMessage() : "No se puede eliminar el producto porque está asociado a ventas u otros registros.");
        }
    }

    private Map<String, Object> buildTableModel(Integer page, Integer size, String mensaje, String error) {
        int pageNum = (page != null && page >= 0) ? page : 0;
        int sizeNum = (size != null && size > 0) ? size : 10;

        Page<ProductoDto> pagina = productoService.findPage(pageNum, sizeNum);
        long totalElements = pagina.getTotalSize();
        int startItem = totalElements == 0 ? 0 : (pagina.getPageNumber() * pagina.getSize() + 1);
        int endItem = (int) Math.min((long) (pagina.getPageNumber() + 1) * pagina.getSize(), totalElements);

        Map<String, Object> model = new HashMap<>();
        model.put("productos", pagina.getContent());
        model.put("currentPage", pagina.getPageNumber());
        model.put("totalPages", pagina.getTotalPages());
        model.put("totalElements", totalElements);
        model.put("size", pagina.getSize());
        model.put("startItem", startItem);
        model.put("endItem", endItem);
        model.put("baseUrl", "/ui/productos/list");

        if (mensaje != null) model.put("mensaje", mensaje);
        if (error != null) model.put("error", error);

        return model;
    }
}