package com.app.producto.service;

import com.app.exception.BusinessException;
import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.repository.ProductoRepository;
import com.app.producto.model.Producto;
import com.app.venta.repository.VentaDetalleRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final VentaDetalleRepository ventaDetalleRepository;

    public ProductoService(ProductoRepository productoRepository,
                           VentaDetalleRepository ventaDetalleRepository) {
        this.productoRepository = productoRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
    }

    public List<ProductoDto> findAll() {
        return StreamSupport.stream(productoRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ProductoDto> buscar(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll().stream().limit(10).collect(Collectors.toList());
        }
        String pattern = "%" + query.trim() + "%";
        return productoRepository.buscar(pattern).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductoDto> findById(Long id) {
        return productoRepository.findById(id).map(this::mapToDto);
    }

    @Transactional
    public ProductoDto save(ProductoFormDto formDto) {
        Producto producto = mapFormToEntity(formDto);
        Producto saved = productoRepository.save(producto);
        return mapToDto(saved);
    }

    @Transactional
    public Optional<ProductoDto> update(Long id, ProductoFormDto formDto) {
        if (!productoRepository.existsById(id)) {
            return Optional.empty();
        }
        Producto producto = mapFormToEntity(formDto);
        producto.setId(id);
        Producto updated = productoRepository.update(producto);
        return Optional.of(mapToDto(updated));
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<Producto> prodOpt = productoRepository.findById(id);
        if (prodOpt.isPresent()) {
            Producto prod = prodOpt.get();
            long countVentas = ventaDetalleRepository.countByProductoId(id);
            if (countVentas > 0) {
                throw new BusinessException("No se puede eliminar el producto '" + prod.getNombre() + "' porque está registrado en " + countVentas + " factura(s) de venta.");
            }
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

    private Producto mapFormToEntity(ProductoFormDto formDto) {
        Producto producto = new Producto();
        producto.setId(formDto.getId());
        producto.setNombre(formDto.getNombre());
        producto.setSku(formDto.getSku());
        producto.setPrecio(formDto.getPrecio());
        producto.setStock(formDto.getStock());
        producto.setCategoria(formDto.getCategoria());
        return producto;
    }
}
