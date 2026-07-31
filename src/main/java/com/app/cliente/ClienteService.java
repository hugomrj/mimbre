package com.app.cliente;

import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void initDefaultClientes() {
        if (clienteRepository.count() == 0) {
            clienteRepository.save(new Cliente("Juan Pérez", "4567890-1", "0981-111-222", "juan.perez@email.com", "Av. Mariscal López 123"));
            clienteRepository.save(new Cliente("María González", "3456789-2", "0982-333-444", "maria.gonzalez@email.com", "Calle Palma 456"));
            clienteRepository.save(new Cliente("Empresa San José S.A.", "80055566-7", "021-444-555", "contacto@sanjose.com.py", "Av. España 789"));
        }
    }

    public List<ClienteDto> findAll() {
        return StreamSupport.stream(clienteRepository.findAll().spliterator(), false)
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
