package com.app.proveedor;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @Get
    public HttpResponse<List<ProveedorDto>> getAll() {
        return HttpResponse.ok(proveedorService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<ProveedorDto> getById(@PathVariable Long id) {
        return proveedorService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<ProveedorDto> create(@Body ProveedorDto proveedorDto) {
        return HttpResponse.created(proveedorService.save(proveedorDto));
    }

    @Put("/{id}")
    public HttpResponse<ProveedorDto> update(@PathVariable Long id, @Body ProveedorDto proveedorDto) {
        return proveedorService.update(id, proveedorDto)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Delete("/{id}")
    public HttpResponse<?> delete(@PathVariable Long id) {
        if (proveedorService.delete(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
