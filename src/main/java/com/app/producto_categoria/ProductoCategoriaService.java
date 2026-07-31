package com.app.producto_categoria;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProductoCategoriaService {

    private final ProductoCategoriaRepository productoCategoriaRepository;

    public ProductoCategoriaService(ProductoCategoriaRepository productoCategoriaRepository) {
        this.productoCategoriaRepository = productoCategoriaRepository;
    }

    @PostConstruct
    public void initDefaultCategorias() {
        if (productoCategoriaRepository.count() == 0) {
            productoCategoriaRepository.save(new ProductoCategoria("Electrónica", "Dispositivos y gadgets electrónicos"));
            productoCategoriaRepository.save(new ProductoCategoria("Ropa", "Vestimenta y prendas"));
            productoCategoriaRepository.save(new ProductoCategoria("Hogar", "Artículos para el hogar y decoración"));
            productoCategoriaRepository.save(new ProductoCategoria("Alimentos", "Productos alimenticios y bebidas"));
            productoCategoriaRepository.save(new ProductoCategoria("Accesorios", "Accesorios variados"));
        }
    }

    public List<ProductoCategoriaDto> findAll() {
        return StreamSupport.stream(productoCategoriaRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductoCategoriaDto> findById(Long id) {
        return productoCategoriaRepository.findById(id).map(this::mapToDto);
    }

    public ProductoCategoriaDto save(ProductoCategoriaDto dto) {
        ProductoCategoria categoria = mapToEntity(dto);
        ProductoCategoria saved = productoCategoriaRepository.save(categoria);
        return mapToDto(saved);
    }

    public Optional<ProductoCategoriaDto> update(Long id, ProductoCategoriaDto dto) {
        if (!productoCategoriaRepository.existsById(id)) {
            return Optional.empty();
        }
        ProductoCategoria categoria = mapToEntity(dto);
        categoria.setId(id);
        ProductoCategoria updated = productoCategoriaRepository.update(categoria);
        return Optional.of(mapToDto(updated));
    }

    public boolean delete(Long id) {
        if (productoCategoriaRepository.existsById(id)) {
            productoCategoriaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ProductoCategoriaDto mapToDto(ProductoCategoria categoria) {
        ProductoCategoriaDto dto = new ProductoCategoriaDto();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        return dto;
    }

    private ProductoCategoria mapToEntity(ProductoCategoriaDto dto) {
        ProductoCategoria categoria = new ProductoCategoria();
        categoria.setId(dto.getId());
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return categoria;
    }
}
