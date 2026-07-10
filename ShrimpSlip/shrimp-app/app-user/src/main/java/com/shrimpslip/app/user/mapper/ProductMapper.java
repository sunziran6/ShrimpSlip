package com.shrimpslip.app.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shrimpslip.app.user.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /** 原子扣减库存（防止超卖） */
    @Update("UPDATE product SET stock = stock - #{qty}, version = version + 1 WHERE id = #{id} AND stock >= #{qty} AND version = #{version}")
    int decrementStock(@Param("id") Long id, @Param("qty") Integer qty, @Param("version") Integer version);
}
