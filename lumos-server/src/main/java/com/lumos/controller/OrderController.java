package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.*;
import com.lumos.mapper.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// 订单处理：下单、支付、发货、收货、取消、退款
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderInfoMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final CartMapper cartMapper;
    private final UserMapper userMapper;

    public OrderController(OrderInfoMapper orderMapper, OrderItemMapper orderItemMapper,
                           ProductMapper productMapper, AddressMapper addressMapper,
                           CartMapper cartMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.addressMapper = addressMapper;
        this.cartMapper = cartMapper;
        this.userMapper = userMapper;
    }

// 创建订单（立即购买或购物车结算）
// items: [{productId, quantity}]，addressId 为收货地址
    @PostMapping("/create")
    @Transactional
    public R<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long userId = JwtUtils.UserContext.getId();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        Long addressId = Long.valueOf(body.get("addressId").toString());
        String remark = body.get("remark") == null ? null : body.get("remark").toString();

        if (items == null || items.isEmpty()) {
            throw new BizException("请选择要购买的商品");
        }
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BizException("收货地址无效");
        }

        OrderInfo order = new OrderInfo();
        order.setOrderNo(genOrderNo());
        order.setUserId(userId);
        order.setStatus(OrderInfo.STATUS_PENDING);
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getDetail());
        order.setRemark(remark);
        order.setTotalPrice(BigDecimal.ZERO);
        order.setCreateTime(new Date());
        orderMapper.insert(order);

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> m : items) {
            Long productId = Long.valueOf(m.get("productId").toString());
            int quantity = Integer.parseInt(m.get("quantity").toString());
            if (quantity <= 0) {
                throw new BizException("购买数量必须大于 0");
            }
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1) {
                throw new BizException("商品不存在或已下架");
            }
            if (product.getLimitNum() != null && quantity > product.getLimitNum()) {
                throw new BizException("商品【" + product.getName() + "】每人限购 " + product.getLimitNum() + " 件");
            }
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            orderItemMapper.insert(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        order.setTotalPrice(total);
        orderMapper.updateById(order);

        // 购物车结算时移除对应商品
        if (Boolean.TRUE.equals(body.get("fromCart"))) {
            List<Long> productIds = items.stream()
                    .map(m -> Long.valueOf(m.get("productId").toString())).collect(Collectors.toList());
            cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId).in("product_id", productIds));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalPrice", total);
        return R.ok(result);
    }

// 余额支付
    @PostMapping("/pay/{id}")
    @Transactional
    public R<Void> pay(@PathVariable Long id) {
        Long userId = JwtUtils.UserContext.getId();
        OrderInfo order = getOwnOrder(id, userId);
        if (order.getStatus() != OrderInfo.STATUS_PENDING) {
            throw new BizException("订单状态不正确，无法支付");
        }
        User user = userMapper.selectById(userId);
        if (user.getBalance().compareTo(order.getTotalPrice()) < 0) {
            throw new BizException("余额不足，请先充值");
        }
        // 校验并扣减库存
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", id));
        for (OrderItem item : items) {
            int rows = productMapper.update(null, new UpdateWrapper<Product>()
                    .eq("id", item.getProductId())
                    .ge("stock", item.getQuantity())
                    .setSql("stock = stock - " + item.getQuantity()));
            if (rows == 0) {
                throw new BizException("商品【" + item.getProductName() + "】库存不足");
            }
        }
        user.setBalance(user.getBalance().subtract(order.getTotalPrice()));
        userMapper.updateById(user);
        order.setStatus(OrderInfo.STATUS_PAID);
        order.setPayTime(new Date());
        orderMapper.updateById(order);
        return R.ok();
    }

// 我的订单
    @GetMapping("/my")
    public R<List<Map<String, Object>>> my(@RequestParam(required = false) Integer status) {
        Long userId = JwtUtils.UserContext.getId();
        QueryWrapper<OrderInfo> qw = new QueryWrapper<OrderInfo>().eq("user_id", userId);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("id");
        return R.ok(withItems(orderMapper.selectList(qw)));
    }

// 订单详情（本人或管理员）
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        OrderInfo order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        boolean isOwner = order.getUserId().equals(JwtUtils.UserContext.getId());
        boolean isAdmin = "ADMIN".equals(JwtUtils.UserContext.getRole());
        if (!isOwner && !isAdmin) {
            throw new BizException(403, "无权限查看该订单");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("order", order);
        map.put("items", orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", id)));
        return R.ok(map);
    }

// 取消订单（仅待支付）
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        OrderInfo order = getOwnOrder(id, JwtUtils.UserContext.getId());
        if (order.getStatus() != OrderInfo.STATUS_PENDING) {
            throw new BizException("当前状态不允许取消");
        }
        order.setStatus(OrderInfo.STATUS_CANCELLED);
        orderMapper.updateById(order);
        return R.ok();
    }

// 确认收货
    @PutMapping("/confirm/{id}")
    public R<Void> confirm(@PathVariable Long id) {
        OrderInfo order = getOwnOrder(id, JwtUtils.UserContext.getId());
        if (order.getStatus() != OrderInfo.STATUS_SHIPPED) {
            throw new BizException("订单尚未发货");
        }
        order.setStatus(OrderInfo.STATUS_FINISHED);
        order.setFinishTime(new Date());
        orderMapper.updateById(order);
        return R.ok();
    }

    // ==================== 管理员 ====================

// 订单分页（按状态/订单号筛选）
    @GetMapping("/page")
    public R<Page<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) String orderNo) {
        checkAdmin();
        QueryWrapper<OrderInfo> qw = new QueryWrapper<>();
        if (status != null) {
            qw.eq("status", status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            qw.like("order_no", orderNo);
        }
        qw.orderByDesc("id");
        Page<OrderInfo> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<Map<String, Object>> records = withItems(page.getRecords());
        Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return R.ok(result);
    }

// 确认发货
    @PutMapping("/ship/{id}")
    public R<Void> ship(@PathVariable Long id) {
        checkAdmin();
        OrderInfo order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != OrderInfo.STATUS_PAID) {
            throw new BizException("仅已支付订单可以发货");
        }
        order.setStatus(OrderInfo.STATUS_SHIPPED);
        order.setShipTime(new Date());
        orderMapper.updateById(order);
        return R.ok();
    }

// 退款：退回余额并恢复库存
    @PutMapping("/refund/{id}")
    @Transactional
    public R<Void> refund(@PathVariable Long id) {
        checkAdmin();
        OrderInfo order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != OrderInfo.STATUS_PAID && order.getStatus() != OrderInfo.STATUS_SHIPPED) {
            throw new BizException("当前状态不允许退款");
        }
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            user.setBalance(user.getBalance().add(order.getTotalPrice()));
            userMapper.updateById(user);
        }
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", id));
        for (OrderItem item : items) {
            productMapper.update(null, new UpdateWrapper<Product>()
                    .eq("id", item.getProductId())
                    .setSql("stock = stock + " + item.getQuantity()));
        }
        order.setStatus(OrderInfo.STATUS_REFUNDED);
        orderMapper.updateById(order);
        return R.ok();
    }

    private OrderInfo getOwnOrder(Long id, Long userId) {
        OrderInfo order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private List<Map<String, Object>> withItems(List<OrderInfo> orders) {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> orderIds = orders.stream().map(OrderInfo::getId).collect(Collectors.toList());
        Map<Long, List<OrderItem>> itemMap = orderItemMapper.selectList(
                        new QueryWrapper<OrderItem>().in("order_id", orderIds))
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderInfo order : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("order", order);
            map.put("items", itemMap.getOrDefault(order.getId(), new ArrayList<>()));
            result.add(map);
        }
        return result;
    }

    private String genOrderNo() {
        return "LM" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + (int) (Math.random() * 900 + 100);
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
