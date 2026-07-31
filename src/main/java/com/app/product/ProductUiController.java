package com.app.product;

import io.micronaut.http.annotation.*;
import io.micronaut.http.MediaType;
import io.micronaut.views.View;
import io.micronaut.core.annotation.Nullable;
import java.util.Map;

@Controller("/ui/products")
public class ProductUiController {

    private final ProductService productService;

    public ProductUiController(ProductService productService) {
        this.productService = productService;
    }

    @View("product/table")
    @Get("/table")
    public Map<String, Object> table() {
        return Map.of("products", productService.findAll());
    }

    @View("product/form")
    @Get("/form{?id}")
    public Map<String, Object> form(@Nullable Long id) {
        if (id != null) {
            return productService.findById(id)
                    .map(product -> (Map<String, Object>) Map.<String, Object>of("product", product))
                    .orElseGet(Map::of);
        }
        return Map.of();
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
