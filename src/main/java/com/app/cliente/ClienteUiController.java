package com.app.cliente;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.Map;

@Controller("/ui/clientes")
public class ClienteUiController {

    private final ClienteService clienteService;

    public ClienteUiController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @View("cliente/table")
    @Get("/table")
    public Map<String, Object> table() {
        return Map.of("clientes", clienteService.findAll());
    }

    @View("cliente/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        if (id != null) {
            return clienteService.findById(id)
                    .map(cliente -> (Map<String, Object>) Map.<String, Object>of("cliente", cliente))
                    .orElseGet(Map::of);
        }
        return Map.of();
    }

    @View("cliente/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Body ClienteDto clienteDto) {
        if (clienteDto.getId() != null) {
            clienteService.update(clienteDto.getId(), clienteDto);
        } else {
            clienteService.save(clienteDto);
        }
        return Map.of("clientes", clienteService.findAll());
    }
}
