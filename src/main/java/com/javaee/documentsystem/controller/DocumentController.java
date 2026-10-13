package com.javaee.documentsystem.controller;

import com.javaee.documentsystem.common.Result;
import com.javaee.documentsystem.entity.Document;
import com.javaee.documentsystem.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    // 获取所有文档（正常列表）
    @GetMapping("/list")
    @Cacheable(value = "documents", key = "'all_' + #session.getId()")
    public Result<List<Document>> list(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        List<Document> list = documentService.findAll(userId);
        return Result.success(list);
    }

    // 添加文档
    @PostMapping("/add")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> add(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 1. 保存文件到服务器
        String filePath = saveFile(file);
        if (filePath == null) {
            return Result.error("文件上传失败");
        }

        // 2. 构建 Document 对象
        Document document = new Document();
        document.setTitle(title);
        document.setType(type);
        document.setAuthor(author);
        document.setFilePath(filePath);
        document.setFileSize((int) file.getSize() / 1024); // 转为 KB
        document.setUserId(userId);

        // 3. 保存到数据库
        boolean success = documentService.add(document);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    // 模糊搜索文档（正常列表）
    @GetMapping("/search")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<List<Document>> search(@RequestParam String keyword, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        List<Document> list = documentService.search(keyword, userId);
        return Result.success(list);
    }

    // 修改文档
    @PutMapping("/update")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> update(@RequestBody Document document, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = documentService.update(document, userId);
        if(success){
            return Result.success("修改成功");
        }else {
            return Result.error("修改失败");
        }
    }

    // 软删除单个文档（移到回收站）
    @DeleteMapping("/delete/{id}")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> delete(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = documentService.delete(id, userId);
       if(success){
           return Result.success("已移至回收站");
       }else {
           return Result.error("删除失败");
       }
    }

    // 批量软删除
    @DeleteMapping("/deleteBatch")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> deleteBatch(@RequestBody List<Integer> ids, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = documentService.deleteBatch(ids, userId);
        if(success){
            return Result.success("已移至回收站");
        }else {
            return Result.error("批量删除失败");
        }
    }

    // 回收站列表
    @GetMapping("/recycle/list")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<List<Document>> recycleList(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        List<Document> list = documentService.findDeleted(userId);
        return Result.success(list);
    }

    // 回收站搜索
    @GetMapping("/recycle/search")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<List<Document>> searchRecycle(@RequestParam String keyword, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        List<Document> list = documentService.searchDeleted(keyword, userId);
        return Result.success(list);
    }

    // 恢复单个文档
    @PutMapping("/recycle/restore/{id}")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> restore(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = documentService.restore(id, userId);
        if (success){
            return Result.success("恢复成功");
        }else {
            return Result.error("恢复失败");
        }
    }

    // 批量恢复
    @PutMapping("/recycle/restoreBatch")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> restoreBatch(@RequestBody List<Integer> ids, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = documentService.restoreBatch(ids, userId);
       if(success){
           return Result.success("批量恢复成功");
       }else {
           return Result.error("批量恢复失败");
       }
    }

    // 永久删除单个文档
    @DeleteMapping("/recycle/permanent/{id}")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> permanentDelete(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = documentService.permanentDelete(id, userId);
        if (success){
            return Result.success("永久删除成功");
        }else {
            return Result.error("删除失败");
        }
    }

    // 批量永久删除
    @DeleteMapping("/recycle/permanentBatch")
    @CacheEvict(value = "documents", allEntries = true)
    public Result<String> permanentDeleteBatch(@RequestBody List<Integer> ids, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401,"未登录");
        }
        boolean success = documentService.permanentDeleteBatch(ids, userId);
        if (success){
            return Result.success("批量永久删除成功");
        }else {
            return Result.error("批量删除失败");
        }
    }


    //文件上传辅助方法
    private String saveFile(MultipartFile file) {
        try {
            // 获取项目根目录的绝对路径（不会受 Tomcat 影响）
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator;

            // 创建目录（如果不存在）
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    System.err.println("目录创建失败：" + uploadDir);
                    return null;
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + ext;

            // 保存文件
            File targetFile = new File(uploadDir + newFilename);
            file.transferTo(targetFile);

            // 打印确认信息
            System.out.println("✅ 文件保存成功：" + targetFile.getAbsolutePath());

            // 返回相对路径（存数据库用）
            return "/uploads/" + newFilename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

