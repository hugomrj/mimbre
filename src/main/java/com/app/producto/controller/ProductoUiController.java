package com.app.producto.controller;

import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.AjusteStockForm;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.service.ProductoService;
import com.app.producto_categoria.service.ProductoCategoriaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import jakarta.validation.Valid;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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
    @Get("/list{?page,size,q}")
    public Map<String, Object> list(@Nullable @QueryValue("page") Integer page,
                                    @Nullable @QueryValue("size") Integer size,
                                    @Nullable @QueryValue("q") String q) {
        return buildTableModel(page, size, null, null, q);
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

    @View("producto/movimientos")
    @Get("/movimientos{?id}")
    public Map<String, Object> movimientos(@Nullable Long id) {
        if (id == null) {
            return Map.of();
        }
        return productoService.findById(id)
                .map(producto -> Map.<String, Object>of(
                        "producto", producto,
                        "movimientos", productoService.findMovimientos(id)))
                .orElseGet(Map::of);
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
    @Post(uri = "/ajustar", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> ajustar(@Body AjusteStockForm form) {
        try {
            productoService.ajustarStock(form.getProductoId(), form.getCantidad(), form.getMotivo());
            return buildTableModel(0, 10, "Stock ajustado correctamente.", null);
        } catch (Exception e) {
            return buildTableModel(0, 10, null,
                    e.getMessage() != null ? e.getMessage() : "No se pudo ajustar el stock.");
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
            return eliminado
                    ? buildTableModel(0, 10, "Producto desactivado correctamente.", null)
                    : buildTableModel(0, 10, null, "No se encontró el producto a eliminar.");
        } catch (Exception e) {
            return buildTableModel(0, 10, null,
                    e.getMessage() != null ? e.getMessage() : "No se pudo desactivar el producto.");
        }
    }





    private Map<String, Object> buildTableModel(Integer page, Integer size, String mensaje, String error) {
        return buildTableModel(page, size, mensaje, error, null);
    }

    private Map<String, Object> buildTableModel(Integer page, Integer size, String mensaje, String error, String q) {
        int pageNum = (page != null && page >= 0) ? page : 0;
        int sizeNum = (size != null && size > 0) ? size : 10;

        Page<ProductoDto> pagina = productoService.findPage(pageNum, sizeNum, q);
        long totalElements = pagina.getTotalSize();

        // ✅ CÁLCULO MATEMÁTICO ESTRICTO
        int totalPages = (int) Math.ceil((double) totalElements / sizeNum);
        if (totalPages == 0) totalPages = 1;

        int startItem = totalElements == 0 ? 0 : (pageNum * sizeNum) + 1;
        int endItem = (int) Math.min((long) (pageNum + 1) * sizeNum, totalElements);

        Map<String, Object> model = new HashMap<>();
        model.put("productos", pagina.getContent());
        model.put("currentPage", pageNum);
        model.put("totalPages", totalPages); // ✅ Usar esta, NO pagina.getTotalPages()
        model.put("totalElements", totalElements);
        model.put("size", sizeNum);
        model.put("startItem", startItem);
        model.put("endItem", endItem);
        model.put("baseUrl", "/ui/productos/list");
        String qLimpio = q == null ? "" : q.trim();
        model.put("q", qLimpio);
        model.put("qEncoded", URLEncoder.encode(qLimpio, StandardCharsets.UTF_8));

        if (mensaje != null) model.put("mensaje", mensaje);
        if (error != null) model.put("error", error);

        return model;
    }





}