package com.app.cliente.controller;

import com.app.cliente.dto.ClienteDto;
import com.app.cliente.service.ClienteService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;
import jakarta.validation.Valid;

import java.util.HashMap;
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
    @Get("/list{?page,size}")
    public Map<String, Object> list(@Nullable @QueryValue("page") Integer page,
                                    @Nullable @QueryValue("size") Integer size) {
        return buildTableModel(page, size, null, null);
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
        String mensaje;
        if (clienteDto.getId() != null) {
            clienteService.update(clienteDto.getId(), clienteDto);
            mensaje = "Cliente actualizado correctamente.";
        } else {
            clienteService.save(clienteDto);
            mensaje = "Cliente registrado correctamente.";
        }
        return buildTableModel(0, 10, mensaje, null);
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
                return buildTableModel(0, 10, "Cliente eliminado correctamente.", null);
            } else {
                return buildTableModel(0, 10, null, "No se encontró el cliente a eliminar.");
            }
        } catch (Exception e) {
            return buildTableModel(0, 10, null,
                    e.getMessage() != null ? e.getMessage() : "No se puede eliminar el cliente porque está asociado a ventas u otros registros.");
        }
    }

    // ✅ MÉTODO CENTRALIZADO PARA PAGINACIÓN (Igual que en Productos y Categorías)
    private Map<String, Object> buildTableModel(Integer page, Integer size, String mensaje, String error) {
        int pageNum = (page != null && page >= 0) ? page : 0;
        int sizeNum = (size != null && size > 0) ? size : 10;

        Page<ClienteDto> pagina = clienteService.findPage(pageNum, sizeNum);
        long totalElements = pagina.getTotalSize();
        int startItem = totalElements == 0 ? 0 : (pagina.getPageNumber() * pagina.getSize() + 1);
        int endItem = (int) Math.min((long) (pagina.getPageNumber() + 1) * pagina.getSize(), totalElements);

        Map<String, Object> model = new HashMap<>();
        model.put("clientes", pagina.getContent());
        model.put("currentPage", pagina.getPageNumber());
        model.put("totalPages", pagina.getTotalPages());
        model.put("totalElements", totalElements);
        model.put("size", pagina.getSize());
        model.put("startItem", startItem);
        model.put("endItem", endItem);
        model.put("baseUrl", "/ui/clientes/list");

        if (mensaje != null) model.put("mensaje", mensaje);
        if (error != null) model.put("error", error);

        return model;
    }
}