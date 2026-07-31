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

    @View("proveedor/table")
    @Get("/table")
    public Map<String, Object> table() {
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
        } else {
            proveedorService.save(proveedorDto);
        }
        return Map.of("proveedores", proveedorService.findAll());
    }
}
