package com.app.proveedor;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

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
    @Get("/list")
    public Map<String, Object> list() {
        return Map.of("proveedores", proveedorService.findAll());
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
    public Map<String, Object> save(@Body ProveedorDto proveedorDto) {
        if (proveedorDto.getId() != null) {
            proveedorService.update(proveedorDto.getId(), proveedorDto);
            return Map.of("proveedores", proveedorService.findAll(), "mensaje", "Proveedor actualizado correctamente.");
        } else {
            proveedorService.save(proveedorDto);
            return Map.of("proveedores", proveedorService.findAll(), "mensaje", "Proveedor registrado correctamente.");
        }
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
                return Map.of("proveedores", proveedorService.findAll(), "mensaje", "Proveedor eliminado correctamente.");
            } else {
                return Map.of("proveedores", proveedorService.findAll(), "error", "No se encontró el proveedor a eliminar.");
            }
        } catch (Exception e) {
            return Map.of("proveedores", proveedorService.findAll(), "error", e.getMessage() != null ? e.getMessage() : "No se puede eliminar el proveedor porque está asociado a otros registros.");
        }
    }
}
