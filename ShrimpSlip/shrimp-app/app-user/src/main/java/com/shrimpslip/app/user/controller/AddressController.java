package com.shrimpslip.app.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shrimpslip.app.user.entity.Address;
import com.shrimpslip.app.user.mapper.AddressMapper;
import com.shrimpslip.common.core.BizException;
import com.shrimpslip.common.core.Result;
import com.shrimpslip.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressMapper addressMapper;

    @GetMapping
    public Result<List<Address>> list() {
        Long userId = UserContext.get();
        List<Address> list = addressMapper.selectList(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        return Result.ok(list);
    }

    @PostMapping
    public Result<Address> create(@RequestBody Address address) {
        Long userId = UserContext.get();
        address.setId(null);
        address.setUserId(userId);
        address.setCreatedAt(LocalDateTime.now());
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearOtherDefaults(userId);
        }
        addressMapper.insert(address);
        return Result.ok(address);
    }

    @PutMapping("/{id}")
    public Result<Address> update(@PathVariable Long id, @RequestBody Address address) {
        Long userId = UserContext.get();
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BizException(404, "地址不存在");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearOtherDefaults(userId);
        }
        address.setId(id);
        address.setUserId(userId);
        addressMapper.updateById(address);
        return Result.ok(address);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.get();
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BizException(404, "地址不存在");
        }
        addressMapper.deleteById(id);
        return Result.ok();
    }

    private void clearOtherDefaults(Long userId) {
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
    }
}
