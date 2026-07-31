package com.app.category;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void initDefaultCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Electrónica", "Dispositivos y gadgets electrónicos"));
            categoryRepository.save(new Category("Ropa", "Vestimenta y prendas"));
            categoryRepository.save(new Category("Hogar", "Artículos para el hogar y decoración"));
            categoryRepository.save(new Category("Alimentos", "Productos alimenticios y bebidas"));
            categoryRepository.save(new Category("Accesorios", "Accesorios variados"));
        }
    }

    public List<CategoryDto> findAll() {
        return StreamSupport.stream(categoryRepository.findAll().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDto> findById(Long id) {
        return categoryRepository.findById(id).map(this::mapToDto);
    }

    public CategoryDto save(CategoryDto dto) {
        Category category = mapToEntity(dto);
        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    public Optional<CategoryDto> update(Long id, CategoryDto dto) {
        if (!categoryRepository.existsById(id)) {
            return Optional.empty();
        }
        Category category = mapToEntity(dto);
        category.setId(id);
        Category updated = categoryRepository.update(category);
        return Optional.of(mapToDto(updated));
    }

    public boolean delete(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    private Category mapToEntity(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}
