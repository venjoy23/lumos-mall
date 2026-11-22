package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.MD5Utils;
import com.lumos.common.R;
import com.lumos.entity.Admin;
import com.lumos.mapper.AdminMapper;
import org.springframework.web.bind.annotation.*;

// 管理员信息管理
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMapper adminMapper;

    public AdminController(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @GetMapping("/page")
    public R<Page<Admin>> page(@RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String username) {
        checkAdmin();
        QueryWrapper<Admin> qw = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            qw.like("username", username);
        }
        qw.orderByDesc("id");
        Page<Admin> page = adminMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        page.getRecords().forEach(a -> a.setPassword(null));
        return R.ok(page);
    }

    @PostMapping
    public R<Void> add(@RequestBody Admin admin) {
        checkAdmin();
        if (admin.getUsername() == null || admin.getPassword() == null) {
            throw new BizException("账号和密码不能为空");
        }
        Long count = adminMapper.selectCount(new QueryWrapper<Admin>().eq("username", admin.getUsername()));
        if (count > 0) {
            throw new BizException("该用户名已存在");
        }
        admin.setId(null);
        admin.setPassword(MD5Utils.encrypt(admin.getPassword()));
        adminMapper.insert(admin);
        return R.ok();
    }

    @PutMapping
    public R<Void> update(@RequestBody Admin admin) {
        checkAdmin();
        Admin old = adminMapper.selectById(admin.getId());
        if (old == null) {
            throw new BizException("管理员不存在");
        }
        old.setName(admin.getName());
        old.setPhone(admin.getPhone());
        old.setAvatar(admin.getAvatar());
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            old.setPassword(MD5Utils.encrypt(admin.getPassword()));
        }
        adminMapper.updateById(old);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkAdmin();
        Long currentId = JwtUtils.UserContext.getId();
        if (id.equals(currentId)) {
            throw new BizException("不能删除当前登录的管理员");
        }
        adminMapper.deleteById(id);
        return R.ok();
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
