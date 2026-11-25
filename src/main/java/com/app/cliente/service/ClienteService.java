package com.app.cliente.service;

import com.app.cliente.dto.ClienteDto;
import com.app.cliente.model.Cliente;
import com.app.cliente.repository.ClienteRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteDto> findAll() {
        return StreamSupport.stream(clienteRepository.findAllByActivoTrue().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ NUEVO MÉTODO CON PAGINACIÓN
    public Page<ClienteDto> findPage(int page, int size) {
        return findPage(page, size, null);
    }

    public Page<ClienteDto> findPage(int page, int size, String q) {
        Pageable pageable = Pageable.from(page, size, Sort.of(Sort.Order.desc("id")));
        Page<Cliente> pagina = (q == null || q.trim().isEmpty())
                ? clienteRepository.findAllByActivoTrue(pageable)
                : clienteRepository.buscarPaginado("%" + q.trim() + "%", Pageable.from(page, size));
        return pagina.map(this::mapToDto);
    }

    public List<ClienteDto> buscar(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll().stream().limit(10).collect(Collectors.toList());
        }
        String pattern = "%" + query.trim() + "%";
        return clienteRepository.buscar(pattern).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ClienteDto> findById(Long id) {
        return clienteRepository.findById(id).map(this::mapToDto);
    }

    @Transactional
    public ClienteDto save(ClienteDto dto) {
        Cliente cliente = mapToEntity(dto);
        Cliente saved = clienteRepository.save(cliente);
        return mapToDto(saved);
    }

    @Transactional
    public Optional<ClienteDto> update(Long id, ClienteDto dto) {
        if (!clienteRepository.existsById(id)) {
            return Optional.empty();
        }
        Cliente cliente = mapToEntity(dto);
        cliente.setId(id);
        clienteRepository.findById(id).ifPresent(actual -> cliente.setActivo(actual.isActivo()));
        Cliente updated = clienteRepository.update(cliente);
        return Optional.of(mapToDto(updated));
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<Cliente> cliOpt = clienteRepository.findById(id);
        if (cliOpt.isPresent()) {
            Cliente cli = cliOpt.get();
            cli.setActivo(false);
            clienteRepository.update(cli);
            return true;
        }
        return false;
    }

    private ClienteDto mapToDto(Cliente cliente) {
        ClienteDto dto = new ClienteDto();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setRucDocumento(cliente.getRucDocumento());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setDireccion(cliente.getDireccion());
        return dto;
    }

    private String normalizar(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    private Cliente mapToEntity(ClienteDto dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setRucDocumento(normalizar(dto.getRucDocumento()));
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setDireccion(dto.getDireccion());
        return cliente;
    }
}