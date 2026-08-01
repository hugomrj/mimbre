package com.app.cliente.controller;

import com.app.cliente.dto.ClienteDto;
import com.app.cliente.ClienteService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Controller("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Get
    public HttpResponse<List<ClienteDto>> getAll() {
        return HttpResponse.ok(clienteService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<ClienteDto> getById(@PathVariable Long id) {
        return clienteService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<ClienteDto> create(@Valid @Body ClienteDto clienteDto) {
        return HttpResponse.created(clienteService.save(clienteDto));
    }

    @Put("/{id}")
    public HttpResponse<ClienteDto> update(@PathVariable Long id, @Valid @Body ClienteDto clienteDto) {
        return clienteService.update(id, clienteDto)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Delete("/{id}")
    public HttpResponse<?> delete(@PathVariable Long id) {
        if (clienteService.delete(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
