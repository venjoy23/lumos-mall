package com.lumos.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumos.common.BizException;
import com.lumos.common.JwtUtils;
import com.lumos.common.R;
import com.lumos.entity.News;
import com.lumos.entity.NewsType;
import com.lumos.mapper.NewsMapper;
import com.lumos.mapper.NewsTypeMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 商城资讯（分类 + 内容）
@RestController
@RequestMapping("/api")
public class NewsController {

    private final NewsMapper newsMapper;
    private final NewsTypeMapper newsTypeMapper;

    public NewsController(NewsMapper newsMapper, NewsTypeMapper newsTypeMapper) {
        this.newsMapper = newsMapper;
        this.newsTypeMapper = newsTypeMapper;
    }

    // ==================== 分类 ====================

    @GetMapping("/newsType/list")
    public R<List<NewsType>> typeList() {
        return R.ok(newsTypeMapper.selectList(new QueryWrapper<NewsType>().orderByDesc("id")));
    }

    @PostMapping("/newsType")
    public R<Void> typeAdd(@RequestBody NewsType type) {
        checkAdmin();
        if (type.getName() == null || type.getName().isEmpty()) {
            throw new BizException("分类名称不能为空");
        }
        type.setId(null);
        newsTypeMapper.insert(type);
        return R.ok();
    }

    @PutMapping("/newsType")
    public R<Void> typeUpdate(@RequestBody NewsType type) {
        checkAdmin();
        newsTypeMapper.updateById(type);
        return R.ok();
    }

    @DeleteMapping("/newsType/{id}")
    public R<Void> typeDelete(@PathVariable Long id) {
        checkAdmin();
        newsTypeMapper.deleteById(id);
        return R.ok();
    }

    // ==================== 资讯 ====================

    @GetMapping("/news/page")
    public R<Page<News>> page(@RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(defaultValue = "10") int pageSize,
                              @RequestParam(required = false) Long typeId,
                              @RequestParam(required = false) String title) {
        QueryWrapper<News> qw = new QueryWrapper<>();
        if (typeId != null) {
            qw.eq("type_id", typeId);
        }
        if (title != null && !title.isEmpty()) {
            qw.like("title", title);
        }
        qw.orderByDesc("id");
        return R.ok(newsMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/news/detail/{id}")
    public R<News> detail(@PathVariable Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BizException("资讯不存在");
        }
        return R.ok(news);
    }

    @PostMapping("/news")
    public R<Void> add(@RequestBody News news) {
        checkAdmin();
        if (news.getTitle() == null || news.getTitle().isEmpty()) {
            throw new BizException("标题不能为空");
        }
        news.setId(null);
        newsMapper.insert(news);
        return R.ok();
    }

    @PutMapping("/news")
    public R<Void> update(@RequestBody News news) {
        checkAdmin();
        newsMapper.updateById(news);
        return R.ok();
    }

    @DeleteMapping("/news/{id}")
    public R<Void> delete(@PathVariable Long id) {
        checkAdmin();
        newsMapper.deleteById(id);
        return R.ok();
    }

    private void checkAdmin() {
        if (!"ADMIN".equals(JwtUtils.UserContext.getRole())) {
            throw new BizException(403, "无权限操作");
        }
    }
}
