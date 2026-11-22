package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Cart;
import com.lumos.entity.Product;
import com.lumos.mapper.CartMapper;
import com.lumos.mapper.ProductMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// 购物车
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartController(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

// 我的购物车（含商品信息）
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        Long userId = JwtUtils.UserContext.getId();
        List<Cart> carts = cartMapper.selectList(new QueryWrapper<Cart>().eq("user_id", userId).orderByDesc("id"));
        if (carts.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        Map<Long, Product> productMap = productMapper.selectBatchIds(
                        carts.stream().map(Cart::getProductId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Product::getId, p -> p));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Cart cart : carts) {
            Product p = productMap.get(cart.getProductId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", cart.getId());
            item.put("productId", cart.getProductId());
            item.put("quantity", cart.getQuantity());
            item.put("createTime", cart.getCreateTime());
            if (p != null) {
                item.put("name", p.getName());
                item.put("image", p.getImage());
                item.put("price", p.getPrice());
                item.put("stock", p.getStock());
                item.put("status", p.getStatus());
            }
            result.add(item);
        }
        return R.ok(result);
    }

// 加入购物车
    @PostMapping
    public R<Void> add(@RequestBody Map<String, Object> body) {
        Long userId = JwtUtils.UserContext.getId();
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = body.get("quantity") == null ? 1 : Integer.parseInt(body.get("quantity").toString());
        if (quantity <= 0) {
            throw new BizException("数量必须大于 0");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        Cart exist = cartMapper.selectOne(new QueryWrapper<Cart>()
                .eq("user_id", userId).eq("product_id", productId));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + quantity);
            cartMapper.updateById(exist);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cartMapper.insert(cart);
        }
        return R.ok();
    }

// 修改数量
    @PutMapping("/{id}")
    public R<Void> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Long userId = JwtUtils.UserContext.getId();
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BizException("购物车记录不存在");
        }
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity <= 0) {
            throw new BizException("数量必须大于 0");
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = JwtUtils.UserContext.getId();
        cartMapper.delete(new QueryWrapper<Cart>().eq("id", id).eq("user_id", userId));
        return R.ok();
    }

    @DeleteMapping("/clear")
    public R<Void> clear() {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", JwtUtils.UserContext.getId()));
        return R.ok();
    }
}
