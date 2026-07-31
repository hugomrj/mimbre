package com.app.cliente;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import jakarta.validation.Valid;

import java.util.Map;

@Controller("/ui/clientes")
public class ClienteUiController {

    private final ClienteService clienteService;

    public ClienteUiController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @View("cliente/results")
    @Get("/buscar-cliente{?q}")
    public Map<String, Object> buscarCliente(@Nullable String q) {
        return Map.of("clientes", clienteService.buscar(q));
    }

    @View("cliente/table")
    @Get("/list")
    public Map<String, Object> list() {
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
    public Map<String, Object> save(@Valid @Body ClienteDto clienteDto) {
        if (clienteDto.getId() != null) {
            clienteService.update(clienteDto.getId(), clienteDto);
            return Map.of("clientes", clienteService.findAll(), "mensaje", "Cliente actualizado correctamente.");
        } else {
            clienteService.save(clienteDto);
            return Map.of("clientes", clienteService.findAll(), "mensaje", "Cliente registrado correctamente.");
        }
    }

    @View("cliente/table")
    @Post(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deletePost(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("cliente/table")
    @Delete(uri = "/delete/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteDelete(@PathVariable Long id) {
        return performDelete(id);
    }

    @View("cliente/table")
    @Delete(uri = "/{id}", consumes = MediaType.ALL)
    public Map<String, Object> deleteById(@PathVariable Long id) {
        return performDelete(id);
    }

    private Map<String, Object> performDelete(Long id) {
        try {
            boolean eliminado = clienteService.delete(id);
            if (eliminado) {
                return Map.of("clientes", clienteService.findAll(), "mensaje", "Cliente eliminado correctamente.");
            } else {
                return Map.of("clientes", clienteService.findAll(), "error", "No se encontró el cliente a eliminar.");
            }
        } catch (Exception e) {
            return Map.of("clientes", clienteService.findAll(), "error", e.getMessage() != null ? e.getMessage() : "No se puede eliminar el cliente porque está asociado a ventas u otros registros.");
        }
    }
}
