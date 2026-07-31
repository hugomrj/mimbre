package com.app.producto;

import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoDto> findAll() {
        return StreamSupport.stream(productoRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductoDto> findById(Long id) {
        return productoRepository.findById(id).map(this::mapToDto);
    }

    public ProductoDto save(ProductoDto dto) {
        Producto producto = mapToEntity(dto);
        Producto saved = productoRepository.save(producto);
        return mapToDto(saved);
    }

    public Optional<ProductoDto> update(Long id, ProductoDto dto) {
        if (!productoRepository.existsById(id)) {
            return Optional.empty();
        }
        Producto producto = mapToEntity(dto);
        producto.setId(id);
        Producto updated = productoRepository.update(producto);
        return Optional.of(mapToDto(updated));
    }

    public boolean delete(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ProductoDto mapToDto(Producto producto) {
        ProductoDto dto = new ProductoDto();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setSku(producto.getSku());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCategoria(producto.getCategoria());
        return dto;
    }

    private Producto mapToEntity(ProductoDto dto) {
        Producto producto = new Producto();
        producto.setId(dto.getId());
        producto.setNombre(dto.getNombre());
        producto.setSku(dto.getSku());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(dto.getCategoria());
        return producto;
    }
}
