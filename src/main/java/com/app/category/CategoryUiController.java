package com.app.category;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.views.View;

import java.util.Map;

@Controller("/ui/categories")
public class CategoryUiController {

    private final CategoryService categoryService;

    public CategoryUiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @View("category/table")
    @Get("/table")
    public Map<String, Object> table() {
        return Map.of("categories", categoryService.findAll());
    }

    @View("category/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        if (id != null) {
            return categoryService.findById(id)
                    .map(category -> (Map<String, Object>) Map.<String, Object>of("category", category))
                    .orElseGet(Map::of);
        }
        return Map.of();
    }

    @View("category/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Body CategoryDto categoryDto) {
        if (categoryDto.getId() != null) {
            categoryService.update(categoryDto.getId(), categoryDto);
        } else {
            categoryService.save(categoryDto);
        }
        return Map.of("categories", categoryService.findAll());
    }
}
