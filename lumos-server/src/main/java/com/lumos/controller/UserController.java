package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.MD5Utils;
import com.lumos.common.R;
import com.lumos.entity.User;
import com.lumos.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 用户信息维护（个人中心 + 管理员用户管理）
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

// 当前登录用户信息
    @GetMapping("/profile")
    public R<User> profile() {
        User user = userMapper.selectById(JwtUtils.UserContext.getId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(null);
        return R.ok(user);
    }

// 修改个人资料
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody User body) {
        Long id = JwtUtils.UserContext.getId();
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setName(body.getName());
        user.setPhone(body.getPhone());
        user.setEmail(body.getEmail());
        user.setAvatar(body.getAvatar());
        userMapper.updateById(user);
        return R.ok();
    }

// 修改密码
    @PutMapping("/password")
    public R<Void> updatePassword(@RequestBody Map<String, String> body) {
        Long id = JwtUtils.UserContext.getId();
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!user.getPassword().equals(MD5Utils.encrypt(body.get("oldPassword")))) {
            throw new BizException("原密码错误");
        }
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            throw new BizException("新密码长度不能少于 6 位");
        }
        user.setPassword(MD5Utils.encrypt(newPassword));
        userMapper.updateById(user);
        return R.ok();
    }

    // ==================== 管理员：用户管理 ====================

    @GetMapping("/page")
    public R<Page<User>> page(@RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(defaultValue = "10") int pageSize,
                              @RequestParam(required = false) String username) {
        checkAdmin();
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            qw.like("username", username);
        }
        qw.orderByDesc("id");
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        page.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(page);
    }

    @PostMapping
    public R<Void> add(@RequestBody User user) {
        checkAdmin();
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new BizException("账号和密码不能为空");
        }
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (count > 0) {
            throw new BizException("该用户名已存在");
        }
        user.setId(null);
        user.setPassword(MD5Utils.encrypt(user.getPassword()));
        userMapper.insert(user);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody User user) {
        checkAdmin();
        User old = userMapper.selectById(user.getId());
        if (old == null) {
            throw new BizException("用户不存在");
        }
        old.setName(user.getName());
        old.setPhone(user.getPhone());
        old.setEmail(user.getEmail());
        old.setAvatar(user.getAvatar());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            old.setPassword(MD5Utils.encrypt(user.getPassword()));
        }
        userMapper.updateById(old);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkAdmin();
        userMapper.deleteById(id);
        return R.ok();
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
