package com.app.proveedor;

import com.app.compra.repository.CompraRepository;
import com.app.exception.BusinessException;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;

    public ProveedorService(ProveedorRepository proveedorRepository, CompraRepository compraRepository) {
        this.proveedorRepository = proveedorRepository;
        this.compraRepository = compraRepository;
    }

    public List<ProveedorDto> findAll() {
        return StreamSupport.stream(proveedorRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ProveedorDto> buscar(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll().stream().limit(10).collect(Collectors.toList());
        }
        String pattern = "%" + query.trim() + "%";
        return proveedorRepository.buscar(pattern).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProveedorDto> findById(Long id) {
        return proveedorRepository.findById(id).map(this::mapToDto);
    }

    @Transactional
    public ProveedorDto save(ProveedorDto dto) {
        Proveedor proveedor = mapToEntity(dto);
        Proveedor saved = proveedorRepository.save(proveedor);
        return mapToDto(saved);
    }

    @Transactional
    public Optional<ProveedorDto> update(Long id, ProveedorDto dto) {
        if (!proveedorRepository.existsById(id)) {
            return Optional.empty();
        }
        Proveedor proveedor = mapToEntity(dto);
        proveedor.setId(id);
        Proveedor updated = proveedorRepository.update(proveedor);
        return Optional.of(mapToDto(updated));
    }

    @Transactional
    public boolean delete(Long id) {
        if (proveedorRepository.existsById(id)) {
            long countCompras = compraRepository.countByProveedorId(id);
            if (countCompras > 0) {
                throw new BusinessException("No se puede eliminar el proveedor '" + proveedorRepository.findById(id).map(Proveedor::getNombre).orElse("") + "' porque cuenta con " + countCompras + " compra(s) registrada(s).");
            }
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
