package com.app.cliente;

import com.app.cliente.dto.ClienteDto;
import com.app.cliente.model.Cliente;
import com.app.exception.BusinessException;
import com.app.venta.repository.VentaRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    public ClienteService(ClienteRepository clienteRepository, VentaRepository ventaRepository) {
        this.clienteRepository = clienteRepository;
        this.ventaRepository = ventaRepository;
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
        Cliente updated = clienteRepository.update(cliente);
        return Optional.of(mapToDto(updated));
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<Cliente> cliOpt = clienteRepository.findById(id);
        if (cliOpt.isPresent()) {
            Cliente cli = cliOpt.get();
            long countVentas = ventaRepository.countByClienteId(id);
            if (countVentas > 0) {
                throw new BusinessException("No se puede eliminar el cliente '" + cli.getNombre() + "' porque cuenta con " + countVentas + " venta(s) registrada(s).");
            }
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
