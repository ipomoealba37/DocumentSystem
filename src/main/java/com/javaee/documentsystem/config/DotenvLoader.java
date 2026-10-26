package com.javaee.documentsystem.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 极简 .env 加载器：在 Spring 启动前，把项目根目录 .env 里的键值写进 System properties，
 * 这样 application.properties 里的 {@code ${AI_DEEPSEEK_API_KEY:}} 就能取到值。
 *
 * <p>为什么不用现成库：只需要读几十行 key=value，引入依赖反而增加打包体积和版本风险。
 *
 * <p>优先级：系统环境变量 / 命令行参数 &gt; .env &gt; application.properties 里的默认值。
 * 已经存在的键不会被 .env 覆盖。
 *
 * <p>.env 已被 .gitignore 排除，仓库里只提交 .env.example。
 */
public final class DotenvLoader {

    private DotenvLoader() {
    }

    /** 加载成功返回载入的键名列表（只返回名字，不返回值，避免密钥进日志） */
    public static List<String> load() {
        List<String> loaded = new ArrayList<>();
        Path file = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.isRegularFile(file)) {
            return loaded;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            System.out.println("读取 .env 失败：" + e.getMessage());
            return loaded;
        }
        for (String raw : lines) {
            // 用记事本保存的 UTF-8 会带 BOM，不去掉会让第一个键名变成 "\uFEFFAI_DEEPSEEK_API_KEY" 而读不到
            String line = raw.startsWith("\uFEFF") ? raw.substring(1).trim() : raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("export ")) {
                line = line.substring(7).trim();
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = unquote(line.substring(eq + 1).trim());
            if (key.isEmpty() || System.getenv(key) != null || System.getProperty(key) != null) {
                continue;
            }
            System.setProperty(key, value);
            loaded.add(key);
        }
        return loaded;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
