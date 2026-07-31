package com.app.proveedor;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<ProveedorDto> findAll() {
        return StreamSupport.stream(proveedorRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProveedorDto> findById(Long id) {
        return proveedorRepository.findById(id).map(this::mapToDto);
    }

    public ProveedorDto save(ProveedorDto dto) {
        Proveedor proveedor = mapToEntity(dto);
        Proveedor saved = proveedorRepository.save(proveedor);
        return mapToDto(saved);
    }

    public Optional<ProveedorDto> update(Long id, ProveedorDto dto) {
        if (!proveedorRepository.existsById(id)) {
            return Optional.empty();
        }
        Proveedor proveedor = mapToEntity(dto);
        proveedor.setId(id);
        Proveedor updated = proveedorRepository.update(proveedor);
        return Optional.of(mapToDto(updated));
    }

    public boolean delete(Long id) {
        if (proveedorRepository.existsById(id)) {
            proveedorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ProveedorDto mapToDto(Proveedor proveedor) {
        ProveedorDto dto = new ProveedorDto();
        dto.setId(proveedor.getId());
        dto.setNombre(proveedor.getNombre());
        dto.setRuc(proveedor.getRuc());
        dto.setTelefono(proveedor.getTelefono());
        dto.setEmail(proveedor.getEmail());
        dto.setDireccion(proveedor.getDireccion());
        return dto;
    }

    private Proveedor mapToEntity(ProveedorDto dto) {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(dto.getId());
        proveedor.setNombre(dto.getNombre());
        proveedor.setRuc(dto.getRuc());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setDireccion(dto.getDireccion());
        return proveedor;
    }
}
