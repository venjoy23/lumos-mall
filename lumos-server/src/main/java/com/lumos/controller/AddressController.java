package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Address;
import com.lumos.mapper.AddressMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 收货地址管理
@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressMapper addressMapper;

    public AddressController(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @GetMapping("/list")
    public R<List<Address>> list() {
        return R.ok(addressMapper.selectList(new QueryWrapper<Address>()
                .eq("user_id", JwtUtils.UserContext.getId())
                .orderByDesc("is_default").orderByDesc("id")));
    }

    @PostMapping
    public R<Void> add(@RequestBody Address address) {
        Long userId = JwtUtils.UserContext.getId();
        validate(address);
        address.setId(null);
        address.setUserId(userId);
        if (isDefaultFlag(address)) {
            clearDefault(userId);
        }
        addressMapper.insert(address);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody Address address) {
        Long userId = JwtUtils.UserContext.getId();
        Address old = addressMapper.selectById(address.getId());
        if (old == null || !old.getUserId().equals(userId)) {
            throw new BizException("地址不存在");
        }
        validate(address);
        old.setName(address.getName());
        old.setPhone(address.getPhone());
        old.setDetail(address.getDetail());
        old.setIsDefault(address.getIsDefault());
        if (isDefaultFlag(address)) {
            clearDefault(userId);
            old.setIsDefault(1);
        }
        addressMapper.updateById(old);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        addressMapper.delete(new QueryWrapper<Address>()
                .eq("id", id).eq("user_id", JwtUtils.UserContext.getId()));
        return R.ok();
    }

    private void validate(Address address) {
        if (address.getName() == null || address.getName().isEmpty()
                || address.getPhone() == null || address.getPhone().isEmpty()
                || address.getDetail() == null || address.getDetail().isEmpty()) {
            throw new BizException("收货人、电话、详细地址不能为空");
        }
    }

    private boolean isDefaultFlag(Address address) {
        return address.getIsDefault() != null && address.getIsDefault() == 1;
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null, new UpdateWrapper<Address>()
                .eq("user_id", userId).set("is_default", 0));
    }
}
