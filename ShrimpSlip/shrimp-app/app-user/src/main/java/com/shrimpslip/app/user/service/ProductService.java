package com.shrimpslip.app.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrimpslip.app.user.entity.Product;

public interface ProductService {
    Page<Product> list(int page, int size, String keyword, String category);
    Product getById(Long id);
    Product create(Product product);
    Product update(Product product);
    void delete(Long id);
    boolean decrementStock(Long id, Integer qty, Integer version);
}
