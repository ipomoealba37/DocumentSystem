package com.javaee.documentsystem.service;

import com.javaee.documentsystem.entity.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 大文件分片上传。
 *
 * <p>整体流程：init（建会话、返回分片计划） -> chunk（逐片写入临时目录） ->
 * merge（校验并合并成完整文件、写数据库）。临时目录结构：
 * <pre>
 *   chunk-temp/{uploadId}/meta.properties   本次上传的元信息
 *   chunk-temp/{uploadId}/0.part, 1.part ... 已接收的分片
 * </pre>
 * 分片落在 uploads 之外，避免被 /uploads/** 静态资源直接访问到。
 */
@Service
public class ChunkUploadService {

    private static final Logger log = LoggerFactory.getLogger(ChunkUploadService.class);

    /** uploadId 只允许字母数字下划线中划线，从根上杜绝 ../ 之类的路径穿越 */
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{4,80}$");
    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,64}$");
    private static final String META_FILE = "meta.properties";
    private static final String CHUNK_SUFFIX = ".part";

    @Autowired
    private DocumentService documentService;

    /** 单片的预期大小 */
    @Value("${upload.chunk-size:2MB}")
    private DataSize chunkSize;

    /** 单个文件的大小上限 */
    @Value("${upload.max-file-size:512MB}")
    private DataSize maxFileSize;

    /** 分片临时目录，留空则用 {user.dir}/chunk-temp */
    @Value("${upload.temp-dir:}")
    private String tempDirConfig;

    /** 临时分片保留时长（小时），超时未合并的会被定时清理 */
    @Value("${upload.expire-hours:24}")
    private long expireHours;

    // ==================== 对外接口 ====================

    /**
     * 初始化（或恢复）一次上传。
     *
     * @param fileName 原始文件名，用于取扩展名和前端展示
     * @param fileSize 文件总字节数
     * @param fileKey  前端算出的文件指纹（文件名+大小+修改时间），用于断点续传；非法则退化为随机 id
     * @return 分片计划：uploadId、分片大小、总分片数、已成功上传的分片序号
     */
    public InitResult init(String fileName, long fileSize, String fileKey, Integer userId) throws IOException {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("文件大小不合法");
        }
        if (fileSize > maxFileSize.toBytes()) {
            throw new IllegalArgumentException("文件超过上限（" + readable(maxFileSize.toBytes()) + "）");
        }

        long chunkBytes = chunkSize.toBytes();
        int totalChunks = (int) ((fileSize + chunkBytes - 1) / chunkBytes);

        String uploadId = userId + "_" + (fileKey != null && KEY_PATTERN.matcher(fileKey).matches()
                ? fileKey
                : UUID.randomUUID().toString().replace("-", ""));
        Path dir = chunkDir(uploadId);

        Meta meta = readMeta(dir);
        // 同一个 uploadId 但文件大小对不上，说明前端复用了旧指纹，直接重置
        if (meta != null && meta.fileSize != fileSize) {
            deleteDir(dir);
            meta = null;
        }
        if (meta == null) {
            Files.createDirectories(dir);
            meta = new Meta();
            meta.fileName = fileName == null ? "unnamed" : fileName;
            meta.fileSize = fileSize;
            meta.chunkSize = chunkBytes;
            meta.totalChunks = totalChunks;
            meta.userId = userId;
            writeMeta(dir, meta);
        }

        return new InitResult(uploadId, meta.chunkSize, meta.totalChunks, uploadedIndexes(dir, meta.totalChunks));
    }

    /** 接收一个分片，先写 .tmp 再原子改名，避免半截分片被 merge 当成有效分片 */
    public void saveChunk(String uploadId, int index, MultipartFile chunk, Integer userId) throws IOException {
        Path dir = requireSession(uploadId, userId);
        Meta meta = requireMeta(dir);

        if (index < 0 || index >= meta.totalChunks) {
            throw new IllegalArgumentException("分片序号越界：" + index);
        }
        if (chunk == null || chunk.getSize() <= 0) {
            throw new IllegalArgumentException("分片内容为空");
        }
        if (chunk.getSize() > meta.chunkSize) {
            throw new IllegalArgumentException("分片大小超出限制");
        }
        boolean lastChunk = index == meta.totalChunks - 1;
        if (!lastChunk && chunk.getSize() != meta.chunkSize) {
            throw new IllegalArgumentException("分片大小与约定不一致，请重新上传");
        }

        Path tmp = dir.resolve(index + CHUNK_SUFFIX + ".tmp");
        try (InputStream in = chunk.getInputStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(tmp, dir.resolve(index + CHUNK_SUFFIX), StandardCopyOption.REPLACE_EXISTING);
    }

    /** 校验分片完整性后合并成完整文件，并写入文档记录 */
    public Document merge(String uploadId, String title, String type, String author,
                          String content, Integer userId) throws IOException {
        Path dir = requireSession(uploadId, userId);
        Meta meta = requireMeta(dir);

        long total = 0;
        List<Path> chunks = new ArrayList<>(meta.totalChunks);
        for (int i = 0; i < meta.totalChunks; i++) {
            Path p = dir.resolve(i + CHUNK_SUFFIX);
            if (!Files.exists(p)) {
                throw new IllegalStateException("分片不完整，缺少第 " + (i + 1) + "/" + meta.totalChunks + " 片，请继续上传");
            }
            total += Files.size(p);
            chunks.add(p);
        }
        if (total != meta.fileSize) {
            throw new IllegalStateException("分片总大小与原文件不一致，请重新上传");
        }

        Path targetDir = uploadDir();
        Files.createDirectories(targetDir);
        String finalName = UUID.randomUUID().toString().replace("-", "") + extOf(meta.fileName);
        Path target = targetDir.resolve(finalName);

        // 按序拼接：FileChannel.transferTo 走零拷贝，比逐块读进内存快得多
        try (FileChannel out = FileChannel.open(target,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Path p : chunks) {
                try (FileChannel in = FileChannel.open(p, StandardOpenOption.READ)) {
                    long size = in.size();
                    long pos = 0;
                    while (pos < size) {
                        pos += in.transferTo(pos, size - pos, out);
                    }
                }
            }
        } catch (IOException e) {
            Files.deleteIfExists(target);
            throw e;
        }

        Document document = new Document();
        document.setTitle(title);
        document.setType(type);
        document.setAuthor(author);
        document.setContent(content);
        document.setFilePath("/uploads/" + finalName);
        // 向上取整，小文件不至于显示成 0KB
        document.setFileSize((int) Math.max(1, (meta.fileSize + 1023) / 1024));
        document.setUserId(userId);

        if (!documentService.add(document)) {
            Files.deleteIfExists(target);
            throw new IllegalStateException("保存文档记录失败");
        }

        deleteDir(dir);
        log.info("分片合并完成：{} -> {}（{} 片，{} 字节）", meta.fileName, finalName, meta.totalChunks, total);
        return document;
    }

    /** 放弃上传，清掉临时分片 */
    public void abort(String uploadId, Integer userId) throws IOException {
        deleteDir(requireSession(uploadId, userId));
    }

    /** 每天凌晨清理过期未合并的临时分片 */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanExpired() {
        Path root = tempRoot();
        if (!Files.isDirectory(root)) {
            return;
        }
        long deadline = System.currentTimeMillis() - expireHours * 3600_000L;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
            for (Path dir : ds) {
                if (!Files.isDirectory(dir)) {
                    continue;
                }
                if (Files.getLastModifiedTime(dir).toMillis() < deadline) {
                    deleteDir(dir);
                    log.info("清理过期分片目录：{}", dir.getFileName());
                }
            }
        } catch (IOException e) {
            log.warn("清理过期分片失败", e);
        }
    }

    // ==================== 内部工具 ====================

    private Path requireSession(String uploadId, Integer userId) {
        if (uploadId == null || !ID_PATTERN.matcher(uploadId).matches()) {
            throw new IllegalArgumentException("上传标识不合法");
        }
        Path dir = chunkDir(uploadId);
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("上传会话不存在或已过期，请重新选择文件");
        }
        Meta meta = readMeta(dir);
        if (meta == null) {
            throw new IllegalStateException("上传会话已失效，请重新选择文件");
        }
        if (!meta.userId.equals(userId)) {
            throw new IllegalStateException("无权操作该上传会话");
        }
        return dir;
    }

    private Meta requireMeta(Path dir) {
        Meta meta = readMeta(dir);
        if (meta == null) {
            throw new IllegalStateException("上传会话已失效，请重新选择文件");
        }
        return meta;
    }

    private List<Integer> uploadedIndexes(Path dir, int totalChunks) throws IOException {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < totalChunks; i++) {
            Path p = dir.resolve(i + CHUNK_SUFFIX);
            if (Files.exists(p) && Files.size(p) > 0) {
                list.add(i);
            }
        }
        return list;
    }

    private Meta readMeta(Path dir) {
        Path file = dir.resolve(META_FILE);
        if (!Files.exists(file)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
            Meta meta = new Meta();
            meta.fileName = props.getProperty("fileName", "unnamed");
            meta.fileSize = Long.parseLong(props.getProperty("fileSize", "0"));
            meta.chunkSize = Long.parseLong(props.getProperty("chunkSize", "0"));
            meta.totalChunks = Integer.parseInt(props.getProperty("totalChunks", "0"));
            meta.userId = Integer.parseInt(props.getProperty("userId", "0"));
            return meta;
        } catch (IOException | NumberFormatException e) {
            log.warn("读取分片元信息失败：{}", file, e);
            return null;
        }
    }

    private void writeMeta(Path dir, Meta meta) throws IOException {
        Properties props = new Properties();
        props.setProperty("fileName", meta.fileName);
        props.setProperty("fileSize", String.valueOf(meta.fileSize));
        props.setProperty("chunkSize", String.valueOf(meta.chunkSize));
        props.setProperty("totalChunks", String.valueOf(meta.totalChunks));
        props.setProperty("userId", String.valueOf(meta.userId));
        try (OutputStream out = Files.newOutputStream(dir.resolve(META_FILE))) {
            props.store(out, "chunk upload meta");
        }
    }

    private Path chunkDir(String uploadId) {
        return tempRoot().resolve(uploadId);
    }

    private Path tempRoot() {
        Path base = (tempDirConfig == null || tempDirConfig.isBlank())
                ? Paths.get(System.getProperty("user.dir")).resolve("chunk-temp")
                : Paths.get(tempDirConfig);
        return base.toAbsolutePath().normalize();
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir")).resolve("uploads").toAbsolutePath().normalize();
    }

    private String extOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(dot) : "";
    }

    private void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p : ds) {
                Files.deleteIfExists(p);
            }
        }
        Files.deleteIfExists(dir);
    }

    private String readable(long bytes) {
        if (bytes >= 1024L * 1024 * 1024) {
            return (bytes / (1024L * 1024 * 1024)) + "GB";
        }
        return (bytes / (1024L * 1024)) + "MB";
    }

    private static class Meta {
        String fileName;
        long fileSize;
        long chunkSize;
        int totalChunks;
        Integer userId = 0;
    }

    /** init 的返回体 */
    public record InitResult(String uploadId, long chunkSize, int totalChunks, List<Integer> uploaded) {
    }
}
