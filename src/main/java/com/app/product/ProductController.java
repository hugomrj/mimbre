package com.app.product;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import java.util.List;

@Controller("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Get
    public HttpResponse<List<ProductDto>> getAll() {
        return HttpResponse.ok(productService.findAll());
    }

    @Get("/{id}")
    public HttpResponse<ProductDto> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<ProductDto> create(@Body ProductDto productDto) {
        return HttpResponse.created(productService.save(productDto));
    }

    @Put("/{id}")
    public HttpResponse<ProductDto> update(@PathVariable Long id, @Body ProductDto productDto) {
        return productService.update(id, productDto)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Delete("/{id}")
    public HttpResponse<?> delete(@PathVariable Long id) {
        if (productService.delete(id)) {
            return HttpResponse.noContent();
        }
        return HttpResponse.notFound();
    }
}
