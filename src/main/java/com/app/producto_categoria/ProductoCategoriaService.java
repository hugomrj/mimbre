package com.app.producto_categoria;

import com.app.exception.BusinessException;
import com.app.producto.repository.ProductoRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProductoCategoriaService {

    private final ProductoCategoriaRepository productoCategoriaRepository;
    private final ProductoRepository productoRepository;

    public ProductoCategoriaService(ProductoCategoriaRepository productoCategoriaRepository,
                                    ProductoRepository productoRepository) {
        this.productoCategoriaRepository = productoCategoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<ProductoCategoriaDto> findAll() {
        return StreamSupport.stream(productoCategoriaRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductoCategoriaDto> findById(Long id) {
        return productoCategoriaRepository.findById(id).map(this::mapToDto);
    }

    @Transactional
    public ProductoCategoriaDto save(ProductoCategoriaDto dto) {
        ProductoCategoria categoria = mapToEntity(dto);
        ProductoCategoria saved = productoCategoriaRepository.save(categoria);
        return mapToDto(saved);
    }

    @Transactional
    public Optional<ProductoCategoriaDto> update(Long id, ProductoCategoriaDto dto) {
        if (!productoCategoriaRepository.existsById(id)) {
            return Optional.empty();
        }
        ProductoCategoria categoria = mapToEntity(dto);
        categoria.setId(id);
        ProductoCategoria updated = productoCategoriaRepository.update(categoria);
        return Optional.of(mapToDto(updated));
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<ProductoCategoria> catOpt = productoCategoriaRepository.findById(id);
        if (catOpt.isPresent()) {
            ProductoCategoria cat = catOpt.get();
            long countProductos = productoRepository.countByCategoria(cat.getNombre());
            if (countProductos > 0) {
                throw new BusinessException("No se puede eliminar la categoría '" + cat.getNombre() + "' porque tiene " + countProductos + " producto(s) asociado(s).");
            }
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
