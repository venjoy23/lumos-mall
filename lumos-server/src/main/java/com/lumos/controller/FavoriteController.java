package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Favorite;
import com.lumos.entity.Product;
import com.lumos.entity.User;
import com.lumos.mapper.FavoriteMapper;
import com.lumos.mapper.ProductMapper;
import com.lumos.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// 收藏
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public FavoriteController(FavoriteMapper favoriteMapper, ProductMapper productMapper, UserMapper userMapper) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
    }

// 我的收藏（含商品信息）
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        Long userId = JwtUtils.UserContext.getId();
        List<Favorite> favorites = favoriteMapper.selectList(
                new QueryWrapper<Favorite>().eq("user_id", userId).orderByDesc("id"));
        if (favorites.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        Map<Long, Product> productMap = productMapper.selectBatchIds(
                        favorites.stream().map(Favorite::getProductId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Product::getId, p -> p));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Product p = productMap.get(favorite.getProductId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", favorite.getId());
            item.put("productId", favorite.getProductId());
            item.put("createTime", favorite.getCreateTime());
            if (p != null) {
                item.put("name", p.getName());
                item.put("image", p.getImage());
                item.put("price", p.getPrice());
            }
            result.add(item);
        }
        return R.ok(result);
    }

// 收藏 / 取消收藏（幂等切换）
    @PostMapping("/toggle/{productId}")
    public R<Boolean> toggle(@PathVariable Long productId) {
        Long userId = JwtUtils.UserContext.getId();
        Favorite exist = favoriteMapper.selectOne(new QueryWrapper<Favorite>()
                .eq("user_id", userId).eq("product_id", productId));
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            return R.ok(false);
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteMapper.insert(favorite);
        return R.ok(true);
    }

    @GetMapping("/check/{productId}")
    public R<Boolean> check(@PathVariable Long productId) {
        Long count = favoriteMapper.selectCount(new QueryWrapper<Favorite>()
                .eq("user_id", JwtUtils.UserContext.getId()).eq("product_id", productId));
        return R.ok(count > 0);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        favoriteMapper.delete(new QueryWrapper<Favorite>()
                .eq("id", id).eq("user_id", JwtUtils.UserContext.getId()));
        return R.ok();
    }
}
