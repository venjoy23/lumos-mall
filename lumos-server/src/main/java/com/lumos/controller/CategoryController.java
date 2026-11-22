package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Category;
import com.lumos.mapper.CategoryMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 商品种类管理
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

// 全部分类（前台公开，用于导航）
    @GetMapping("/list")
    public R<List<Category>> list() {
        return R.ok(categoryMapper.selectList(new QueryWrapper<Category>().orderByAsc("sort").orderByAsc("id")));
    }

    @PostMapping
    public R<Void> add(@RequestBody Category category) {
        checkAdmin();
        if (category.getName() == null || category.getName().isEmpty()) {
            throw new BizException("分类名称不能为空");
        }
        category.setId(null);
        categoryMapper.insert(category);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody Category category) {
        checkAdmin();
        categoryMapper.updateById(category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkAdmin();
        categoryMapper.deleteById(id);
        return R.ok();
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
