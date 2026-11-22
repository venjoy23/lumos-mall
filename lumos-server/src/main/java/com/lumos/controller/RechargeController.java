package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Recharge;
import com.lumos.entity.User;
import com.lumos.mapper.RechargeMapper;
import com.lumos.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 账户充值与充值记录
@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    private final RechargeMapper rechargeMapper;
    private final UserMapper userMapper;

    public RechargeController(RechargeMapper rechargeMapper, UserMapper userMapper) {
        this.rechargeMapper = rechargeMapper;
        this.userMapper = userMapper;
    }

// 余额充值（模拟支付，直接到账）
    @PostMapping
    @Transactional
    public R<Void> recharge(@RequestBody Map<String, Object> body) {
        Long userId = JwtUtils.UserContext.getId();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        if (amount.compareTo(new BigDecimal("0.01")) < 0 || amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new BizException("充值金额需在 0.01 - 100000 之间");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", userId).setSql("balance = balance + " + amount));
        Recharge record = new Recharge();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setCreateTime(new Date());
        rechargeMapper.insert(record);
        return R.ok();
    }

// 我的充值记录
    @GetMapping("/my")
    public R<List<Recharge>> my() {
        return R.ok(rechargeMapper.selectList(new QueryWrapper<Recharge>()
                .eq("user_id", JwtUtils.UserContext.getId()).orderByDesc("id")));
    }

// 管理员：全部充值记录分页
    @GetMapping("/page")
    public R<Page<Map<String, Object>>> page(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) String username) {
        checkAdmin();
        QueryWrapper<Recharge> qw = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            List<Long> ids = userMapper.selectList(new QueryWrapper<User>().like("username", username))
                    .stream().map(User::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                return R.ok(new Page<>(pageNum, pageSize));
            }
            qw.in("user_id", ids);
        }
        qw.orderByDesc("id");
        Page<Recharge> page = rechargeMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        Map<Long, String> usernameMap = page.getRecords().isEmpty() ? java.util.Collections.emptyMap()
                : userMapper.selectBatchIds(page.getRecords().stream().map(Recharge::getUserId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getId, User::getUsername));
        Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(r -> {
            Map<String, Object> m = new java.util.HashMap<String, Object>();
            m.put("id", r.getId());
            m.put("amount", r.getAmount());
            m.put("createTime", r.getCreateTime());
            m.put("username", usernameMap.getOrDefault(r.getUserId(), "未知用户"));
            return m;
        }).collect(Collectors.toList()));
        return R.ok(result);
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
