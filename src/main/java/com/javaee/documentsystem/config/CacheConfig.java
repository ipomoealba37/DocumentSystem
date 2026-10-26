package com.javaee.documentsystem.config;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存降级配置：Redis 未启动或连接失败时，不再把异常抛给调用方（表现为 500），
 * 而是记录一条警告，把本次操作当作"缓存未命中 / 写失败"处理，请求照常执行并走数据库。
 *
 * <p>也就是说：Redis 可选。启动 Redis 时文档列表走缓存；没启动时功能完整，只是每次直查数据库。
 * 免去了"必须先启动 Redis 才能跑起来"的门槛，也避免演示时因为忘了开 Redis 而整页报错。
 */
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                warn("读取", cache, key, e);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                warn("写入", cache, key, e);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                warn("失效", cache, key, e);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                System.out.println("[缓存降级] 清空缓存 " + cache.getName() + " 失败，本次跳过：" + e.getMessage());
            }

            private void warn(String op, Cache cache, Object key, RuntimeException e) {
                System.out.println("[缓存降级] Redis " + op + "失败（" + cache.getName() + ", key=" + key
                        + "），本次走数据库：" + e.getMessage());
            }
        };
    }
}
