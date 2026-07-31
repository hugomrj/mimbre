package com.app.product;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDto> findAll() {
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id).map(this::mapToDto);
    }

    public ProductDto save(ProductDto dto) {
        Product product = mapToEntity(dto);
        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public Optional<ProductDto> update(Long id, ProductDto dto) {
        if (!productRepository.existsById(id)) {
            return Optional.empty();
        }
        Product product = mapToEntity(dto);
        product.setId(id);
        Product updated = productRepository.update(product);
        return Optional.of(mapToDto(updated));
    }

    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategory(product.getCategory());
        return dto;
    }

    private Product mapToEntity(ProductDto dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        return product;
    }
}
