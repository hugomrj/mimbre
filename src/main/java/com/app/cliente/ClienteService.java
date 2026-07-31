package com.app.cliente;

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
        return StreamSupport.stream(clienteRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
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

    public ClienteDto save(ClienteDto dto) {
        Cliente cliente = mapToEntity(dto);
        Cliente saved = clienteRepository.save(cliente);
        return mapToDto(saved);
    }

    public Optional<ClienteDto> update(Long id, ClienteDto dto) {
        if (!clienteRepository.existsById(id)) {
            return Optional.empty();
        }
        Cliente cliente = mapToEntity(dto);
        cliente.setId(id);
        Cliente updated = clienteRepository.update(cliente);
        return Optional.of(mapToDto(updated));
    }

    public boolean delete(Long id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
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

    private Cliente mapToEntity(ClienteDto dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setRucDocumento(dto.getRucDocumento());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setDireccion(dto.getDireccion());
        return cliente;
    }
}
