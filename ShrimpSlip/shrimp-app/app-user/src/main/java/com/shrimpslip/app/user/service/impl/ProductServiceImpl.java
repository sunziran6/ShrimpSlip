package com.shrimpslip.app.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shrimpslip.app.user.entity.Product;
import com.shrimpslip.app.user.mapper.ProductMapper;
import com.shrimpslip.app.user.service.ProductService;
import com.shrimpslip.common.core.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<Product> list(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        return productMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        return product;
    }

    @Override
    public Product create(Product product) {
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product update(Product product) {
        Product existing = getById(product.getId());
        product.setVersion(existing.getVersion());
        int rows = productMapper.updateById(product);
        if (rows == 0) {
            throw new BizException(409, "商品已被其他操作修改，请刷新后重试");
        }
        return getById(product.getId());
    }

    @Override
    public void delete(Long id) {
        getById(id);
        productMapper.deleteById(id);
    }

    @Override
    public boolean decrementStock(Long id, Integer qty, Integer version) {
        int rows = productMapper.decrementStock(id, qty, version);
        if (rows == 0) {
            throw new BizException(409, "库存不足或数据已变更，请刷新后重试");
        }
        return true;
    }
}
