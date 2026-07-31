package com.app.product;

import com.app.category.CategoryService;
import io.micronaut.http.annotation.*;
import io.micronaut.http.MediaType;
import io.micronaut.views.View;
import io.micronaut.core.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

@Controller("/ui/products")
public class ProductUiController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductUiController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @View("product/table")
    @Get("/table")
    public Map<String, Object> table() {
        return Map.of("products", productService.findAll());
    }

    @View("product/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        Map<String, Object> model = new HashMap<>();
        model.put("categories", categoryService.findAll());
        if (id != null) {
            productService.findById(id).ifPresent(product -> model.put("product", product));
        }
        return model;
    }

    @View("product/table")
    @Post(uri = "/save", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public Map<String, Object> save(@Body ProductDto productDto) {
        if (productDto.getId() != null) {
            productService.update(productDto.getId(), productDto);
        } else {
            productService.save(productDto);
        }
        // Devuelve la tabla actualizada
        return Map.of("products", productService.findAll());
    }
}
