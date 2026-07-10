package com.shrimpslip.app.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrimpslip.app.user.entity.Product;
import com.shrimpslip.app.user.service.ProductService;
import com.shrimpslip.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.ok(productService.list(page, size, keyword, category));
    }

    @GetMapping("/products/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @PostMapping("/admin/products")
    public Result<Product> create(@RequestBody Product product) {
        return Result.ok(productService.create(product));
    }

    @PutMapping("/admin/products/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return Result.ok(productService.update(product));
    }

    @DeleteMapping("/admin/products/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }
}
