package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Product;
import com.lumos.entity.WarehouseRecord;
import com.lumos.mapper.ProductMapper;
import com.lumos.mapper.WarehouseRecordMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// 商品信息（前台浏览 + 后台维护）与商品入库
@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductMapper productMapper;
    private final WarehouseRecordMapper warehouseRecordMapper;

    public ProductController(ProductMapper productMapper, WarehouseRecordMapper warehouseRecordMapper) {
        this.productMapper = productMapper;
        this.warehouseRecordMapper = warehouseRecordMapper;
    }

// 商品分页查询（支持名称、品牌、价格区间、分类、排序）
    @GetMapping("/product/page")
    public R<Page<Product>> page(@RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String brand,
                                 @RequestParam(required = false) BigDecimal minPrice,
                                 @RequestParam(required = false) BigDecimal maxPrice,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) String sortField,
                                 @RequestParam(required = false) String sortOrder) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        if (name != null && !name.isEmpty()) {
            qw.like("name", name);
        }
        if (brand != null && !brand.isEmpty()) {
            qw.like("brand", brand);
        }
        if (minPrice != null) {
            qw.ge("price", minPrice);
        }
        if (maxPrice != null) {
            qw.le("price", maxPrice);
        }
        if (categoryId != null) {
            qw.eq("category_id", categoryId);
        }
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if ("price".equals(sortField)) {
            qw.orderBy(true, asc, "price");
        } else if ("click".equals(sortField)) {
            qw.orderBy(true, asc, "click_count");
        } else {
            qw.orderByDesc("id");
        }
        return R.ok(productMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

// 热门商品（按点击次数）
    @GetMapping("/product/hot")
    public R<List<Product>> hot(@RequestParam(defaultValue = "8") int limit) {
        Page<Product> page = productMapper.selectPage(new Page<>(1, limit),
                new QueryWrapper<Product>().eq("status", 1).orderByDesc("click_count"));
        return R.ok(page.getRecords());
    }

// 商品详情（点击次数 +1）
    @GetMapping("/product/detail/{id}")
    public R<Product> detail(@PathVariable Long id) {
        productMapper.update(null, new UpdateWrapper<Product>()
                .eq("id", id).setSql("click_count = click_count + 1"));
        Product product = productMapper.selectById(id);
        if (product == null || product.getDeleted() == 1) {
            throw new BizException("商品不存在");
        }
        return R.ok(product);
    }

// 管理端商品分页（不受上架状态限制）
    @GetMapping("/product/adminPage")
    public R<Page<Product>> adminPage(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String brand) {
        checkAdmin();
        QueryWrapper<Product> qw = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            qw.like("name", name);
        }
        if (brand != null && !brand.isEmpty()) {
            qw.like("brand", brand);
        }
        qw.orderByDesc("id");
        return R.ok(productMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @PostMapping("/product")
    public R<Void> add(@RequestBody Product product) {
        checkAdmin();
        validate(product);
        product.setId(null);
        product.setClickCount(0);
        productMapper.insert(product);
        return R.ok();
    }

    @PutMapping("/product")
    public R<Void> update(@RequestBody Product product) {
        checkAdmin();
        validate(product);
        productMapper.updateById(product);
        return R.ok();
    }

    @DeleteMapping("/product/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkAdmin();
        productMapper.deleteById(id);
        return R.ok();
    }

    private void validate(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new BizException("商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("商品价格不合法");
        }
    }

    // ==================== 商品入库 ====================

    @GetMapping("/warehouse/page")
    public R<Page<WarehouseRecord>> warehousePage(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String brand) {
        checkAdmin();
        QueryWrapper<WarehouseRecord> qw = new QueryWrapper<>();
        if ((name != null && !name.isEmpty()) || (brand != null && !brand.isEmpty())) {
            QueryWrapper<Product> pq = new QueryWrapper<>();
            if (name != null && !name.isEmpty()) {
                pq.like("name", name);
            }
            if (brand != null && !brand.isEmpty()) {
                pq.like("brand", brand);
            }
            List<Long> ids = productMapper.selectList(pq).stream().map(Product::getId).collect(java.util.stream.Collectors.toList());
            if (ids.isEmpty()) {
                return R.ok(new Page<>(pageNum, pageSize));
            }
            qw.in("product_id", ids);
        }
        qw.orderByDesc("id");
        return R.ok(warehouseRecordMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

// 入库：新增入库记录并同步增加商品库存
    @PostMapping("/warehouse")
    @Transactional
    public R<Void> warehouseIn(@RequestBody WarehouseRecord record) {
        checkAdmin();
        if (record.getProductId() == null || record.getQuantity() == null || record.getQuantity() <= 0) {
            throw new BizException("请选择商品并填写正确的入库数量");
        }
        Product product = productMapper.selectById(record.getProductId());
        if (product == null) {
            throw new BizException("商品不存在");
        }
        productMapper.update(null, new UpdateWrapper<Product>()
                .eq("id", product.getId()).setSql("stock = stock + " + record.getQuantity()));
        warehouseRecordMapper.insert(record);
        return R.ok();
    }

    @DeleteMapping("/warehouse/{id}")
    public R<Void> warehouseDelete(@PathVariable Long id) {
        checkAdmin();
        warehouseRecordMapper.deleteById(id);
        return R.ok();
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
