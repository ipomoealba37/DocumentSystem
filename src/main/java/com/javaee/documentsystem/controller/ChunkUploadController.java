package com.javaee.documentsystem.controller;

import com.javaee.documentsystem.common.Result;
import com.javaee.documentsystem.entity.Document;
import com.javaee.documentsystem.service.ChunkUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * 大文件分片上传接口。
 * 三步走：init 拿分片计划 -> chunk 逐片上传 -> merge 合并入库。
 */
@RestController
@RequestMapping("/api/document/upload")
public class ChunkUploadController {

    private static final Logger log = LoggerFactory.getLogger(ChunkUploadController.class);

    @Autowired
    private ChunkUploadService chunkUploadService;

    /**
     * 1. 初始化上传：返回 uploadId、分片大小、分片总数、已上传的分片序号（断点续传用）
     */
    @PostMapping("/init")
    public Result<ChunkUploadService.InitResult> init(
            @RequestParam("fileName") String fileName,
            @RequestParam("fileSize") long fileSize,
            @RequestParam(value = "fileKey", required = false) String fileKey,
            HttpSession session) {

        Integer userId = currentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            return Result.success(chunkUploadService.init(fileName, fileSize, fileKey, userId));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("初始化分片上传失败", e);
            return Result.error(500, "初始化上传失败，请重试");
        }
    }

    /**
     * 2. 上传单个分片（前端可并发多片一起传）
     */
    @PostMapping("/chunk")
    public Result<Map<String, Object>> chunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("index") int index,
            @RequestParam("chunk") MultipartFile chunk,
            HttpSession session) {

        Integer userId = currentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            chunkUploadService.saveChunk(uploadId, index, chunk, userId);
            return Result.success(Map.of("index", index));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("保存分片失败：uploadId={}, index={}", uploadId, index, e);
            return Result.error(500, "分片保存失败，请重试");
        }
    }

    /**
     * 3. 合并分片并写入文档记录
     */
    @PostMapping("/merge")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<Document> merge(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "content", required = false) String content,
            HttpSession session) {

        Integer userId = currentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        if (title == null || title.isBlank()) {
            return Result.error(400, "标题不能为空");
        }
        try {
            return Result.success(chunkUploadService.merge(uploadId, title.trim(), type, author, content, userId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("合并分片失败：uploadId={}", uploadId, e);
            return Result.error(500, "合并文件失败，请重试");
        }
    }

    /**
     * 取消上传，清理临时分片
     */
    @PostMapping("/abort")
    public Result<String> abort(@RequestParam("uploadId") String uploadId, HttpSession session) {
        Integer userId = currentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            chunkUploadService.abort(uploadId, userId);
            return Result.success("已取消上传");
        } catch (Exception e) {
            log.warn("取消上传失败：uploadId={}", uploadId, e);
            return Result.error(400, "取消上传失败");
        }
    }

    private Integer currentUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }
}
