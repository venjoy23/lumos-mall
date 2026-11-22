package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Banner;
import com.lumos.mapper.BannerMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 轮播图管理（系统管理）
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    private final BannerMapper bannerMapper;

    public BannerController(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

// 前台：启用中的轮播图（按顺序）
    @GetMapping("/list")
    public R<List<Banner>> list() {
        return R.ok(bannerMapper.selectList(new QueryWrapper<Banner>()
                .eq("status", 1).orderByAsc("sort").orderByAsc("id")));
    }

// 后台：全部轮播图
    @GetMapping("/all")
    public R<List<Banner>> all() {
        checkAdmin();
        return R.ok(bannerMapper.selectList(new QueryWrapper<Banner>().orderByAsc("sort").orderByAsc("id")));
    }

    @PostMapping
    public R<Void> add(@RequestBody Banner banner) {
        checkAdmin();
        banner.setId(null);
        bannerMapper.insert(banner);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody Banner banner) {
        checkAdmin();
        bannerMapper.updateById(banner);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkAdmin();
        bannerMapper.deleteById(id);
        return R.ok();
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
