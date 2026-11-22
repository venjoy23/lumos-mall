package com.lumos.controller;

import com.lumos.common.BizException;
import com.lumos.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 文件上传（图片）
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${lumos.upload.dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BizException("文件不能为空");
        }
        String original = file.getOriginalFilename();
        String suffix = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf(".")) : "";
        if (!suffix.matches("\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
            throw new BizException("仅支持 jpg/png/gif/webp/bmp 图片");
        }
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File dir = new File(uploadDir + dateDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("上传目录创建失败");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + suffix;
        file.transferTo(new File(dir, filename));
        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/" + dateDir + "/" + filename);
        return R.ok(result);
    }
}
