package com.app.category;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Get
    public HttpResponse<List<CategoryDto>> getAll() {
        return HttpResponse.ok(categoryService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<CategoryDto> getById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<CategoryDto> create(@Body CategoryDto categoryDto) {
        return HttpResponse.created(categoryService.save(categoryDto));
    }

    @Put("/{id}")
    public HttpResponse<CategoryDto> update(@PathVariable Long id, @Body CategoryDto categoryDto) {
        return categoryService.update(id, categoryDto)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Delete("/{id}")
    public HttpResponse<?> delete(@PathVariable Long id) {
        if (categoryService.delete(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
