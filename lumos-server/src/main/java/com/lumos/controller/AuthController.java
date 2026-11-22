package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.MD5Utils;
import com.lumos.common.R;
import com.lumos.entity.Admin;
import com.lumos.entity.User;
import com.lumos.mapper.AdminMapper;
import com.lumos.mapper.UserMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// 认证：注册、登录
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final JwtUtils jwtUtils;

    public AuthController(UserMapper userMapper, AdminMapper adminMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.jwtUtils = jwtUtils;
    }

// 用户注册
    @PostMapping("/register")
    public R<Void> register(@RequestBody Map<String, String> body) {
        String username = trim(body.get("username"));
        String password = body.get("password");
        String confirm = body.get("confirmPassword");
        if (username == null || username.length() < 3 || username.length() > 20) {
            throw new BizException("用户名长度需在 3-20 位之间");
        }
        if (password == null || password.length() < 6) {
            throw new BizException("密码长度不能少于 6 位");
        }
        if (!password.equals(confirm)) {
            throw new BizException("两次输入的密码不一致");
        }
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", username));
        if (count > 0) {
            throw new BizException("该用户名已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(MD5Utils.encrypt(password));
        user.setName(trim(body.get("name")));
        user.setPhone(trim(body.get("phone")));
        user.setEmail(trim(body.get("email")));
        user.setBalance(BigDecimal.ZERO);
        userMapper.insert(user);
        return R.ok();
    }

// 登录（role=USER 用户端，role=ADMIN 管理端）
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = trim(body.get("username"));
        String password = body.get("password");
        String role = body.get("role");
        if (username == null || password == null) {
            throw new BizException("请输入账号和密码");
        }
        Map<String, Object> result = new HashMap<>();
        if ("ADMIN".equals(role)) {
            Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", username));
            if (admin == null || !admin.getPassword().equals(MD5Utils.encrypt(password))) {
                throw new BizException("账号或密码错误");
            }
            String token = jwtUtils.generateToken(admin.getId(), admin.getUsername(), "ADMIN");
            admin.setPassword(null);
            result.put("token", token);
            result.put("user", admin);
            result.put("role", "ADMIN");
        } else {
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            if (user == null || !user.getPassword().equals(MD5Utils.encrypt(password))) {
                throw new BizException("账号或密码错误");
            }
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), "USER");
            user.setPassword(null);
            result.put("token", token);
            result.put("user", user);
            result.put("role", "USER");
        }
        return R.ok(result);
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
