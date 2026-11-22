package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.Comment;
import com.lumos.entity.User;
import com.lumos.mapper.CommentMapper;
import com.lumos.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// 商品评论（前台发表，管理员回复/删除）
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public CommentController(CommentMapper commentMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

// 商品评论分页（公开）
    @GetMapping("/page")
    public R<Page<Map<String, Object>>> page(@RequestParam Long productId,
                                             @RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        Page<Comment> page = commentMapper.selectPage(new Page<>(pageNum, pageSize),
                new QueryWrapper<Comment>().eq("product_id", productId).orderByDesc("id"));
        return R.ok(withUser(page));
    }

    @GetMapping("/adminPage")
    public R<Page<Map<String, Object>>> adminPage(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) Long productId) {
        checkAdmin();
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        if (productId != null) {
            qw.eq("product_id", productId);
        }
        qw.orderByDesc("id");
        return R.ok(withUser(commentMapper.selectPage(new Page<>(pageNum, pageSize), qw)));
    }

// 发表评论
    @PostMapping
    public R<Void> add(@RequestBody Comment comment) {
        Long userId = JwtUtils.UserContext.getId();
        if (comment.getProductId() == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BizException("评论内容不能为空");
        }
        comment.setId(null);
        comment.setUserId(userId);
        comment.setReply(null);
        commentMapper.insert(comment);
        return R.ok();
    }

// 管理员回复评论
    @PutMapping("/reply")
    public R<Void> reply(@RequestBody Comment body) {
        checkAdmin();
        Comment comment = commentMapper.selectById(body.getId());
        if (comment == null) {
            throw new BizException("评论不存在");
        }
        comment.setReply(body.getReply());
        commentMapper.updateById(comment);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkAdmin();
        commentMapper.deleteById(id);
        return R.ok();
    }

    private Page<Map<String, Object>> withUser(Page<Comment> page) {
        List<Comment> comments = page.getRecords();
        if (!comments.isEmpty()) {
            Map<Long, String> usernameMap = userMapper.selectBatchIds(
                            comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(User::getId, u -> u.getName() == null ? u.getUsername() : u.getName()));
            List<Map<String, Object>> records = new ArrayList<>();
            for (Comment c : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("productId", c.getProductId());
                map.put("content", c.getContent());
                map.put("reply", c.getReply());
                map.put("createTime", c.getCreateTime());
                map.put("username", usernameMap.getOrDefault(c.getUserId(), "匿名用户"));
                records.add(map);
            }
            Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
            result.setRecords(records);
            return result;
        }
        return new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
