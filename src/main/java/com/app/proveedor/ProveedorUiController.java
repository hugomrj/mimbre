package com.app.proveedor;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@Controller("/ui/proveedores")
public class ProveedorUiController {

    private final ProveedorService proveedorService;

    public ProveedorUiController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @View("proveedor/results")
    @Get("/buscar-proveedor{?q}")
    public Map<String, Object> buscarProveedor(@Nullable String q) {
        return Map.of("proveedores", proveedorService.buscar(q));
    }

    @View("proveedor/table")
    @Get("/list{?page,size}")
    public Map<String, Object> list(@Nullable @QueryValue("page") Integer page,
                                    @Nullable @QueryValue("size") Integer size) {
        return buildTableModel(page, size, null, null);
    }

    @View("proveedor/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        if (id != null) {
            return proveedorService.findById(id)
                    .map(proveedor -> (Map<String, Object>) Map.<String, Object>of("proveedor", proveedor))
                    .orElseGet(Map::of);
        }
        return Map.of();
    }

    @View("proveedor/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Valid @Body ProveedorDto proveedorDto) {
        String mensaje;
        if (proveedorDto.getId() != null) {
            proveedorService.update(proveedorDto.getId(), proveedorDto);
            mensaje = "Proveedor actualizado correctamente.";
        } else {
            proveedorService.save(proveedorDto);
            mensaje = "Proveedor registrado correctamente.";
        }
        return buildTableModel(0, 10, mensaje, null);
    }

    @View("proveedor/table")
    @Post(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deletePost(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("proveedor/table")
    @Delete(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteDelete(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("proveedor/table")
    @Delete(uri = "/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteById(@PathVariable Long id) {
        return performDelete(id);
    }

    private Map<String, Object> performDelete(Long id) {
        try {
            boolean eliminado = proveedorService.delete(id);
            if (eliminado) {
                return buildTableModel(0, 10, "Proveedor eliminado correctamente.", null);
            } else {
                return buildTableModel(0, 10, null, "No se encontró el proveedor a eliminar.");
            }
        } catch (Exception e) {
            return buildTableModel(0, 10, null,
                    e.getMessage() != null ? e.getMessage() : "No se puede eliminar el proveedor porque está asociado a compras u otros registros.");
        }
    }

    private Map<String, Object> buildTableModel(Integer page, Integer size, String mensaje, String error) {
        int pageNum = (page != null && page >= 0) ? page : 0;
        int sizeNum = (size != null && size > 0) ? size : 10;

        Page<ProveedorDto> pagina = proveedorService.findPage(pageNum, sizeNum);
        long totalElements = pagina.getTotalSize();
        int startItem = totalElements == 0 ? 0 : (pagina.getPageNumber() * pagina.getSize() + 1);
        int endItem = (int) Math.min((long) (pagina.getPageNumber() + 1) * pagina.getSize(), totalElements);

        Map<String, Object> model = new HashMap<>();
        model.put("proveedores", pagina.getContent());
        model.put("currentPage", pagina.getPageNumber());
        model.put("totalPages", pagina.getTotalPages());
        model.put("totalElements", totalElements);
        model.put("size", pagina.getSize());
        model.put("startItem", startItem);
        model.put("endItem", endItem);
        model.put("baseUrl", "/ui/proveedores/list");

        if (mensaje != null) model.put("mensaje", mensaje);
        if (error != null) model.put("error", error);

        return model;
    }
}