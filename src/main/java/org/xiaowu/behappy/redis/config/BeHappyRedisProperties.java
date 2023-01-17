package org.xiaowu.behappy.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.xiaowu.behappy.redis.metadata.Constant;

import java.util.List;

/**
 * Redisson 配置属性
 * @author xiaowu
 */
@Data
@ConfigurationProperties(prefix = "behappy.redis")
public class BeHappyRedisProperties {

    private String registerClazzPackage;

    /**
     * @see CacheManager
     * CacheManager配置
     */
    private CacheManager cacheManager = new CacheManager();

    @Data
    static class CacheManager {

        /**
         * cache name前缀
         */
        private String prefix = Constant.CACHE_PREFIX;

        /**
         * cache key , 过期时间
         * @see CacheConfig
         */
        private List<CacheConfig> configs;
    }

    @Data
    static class CacheConfig {
        /**
         * cache key
         */
        private String key;

        /**
         * 单位毫秒
         * 过期时间
         * 默认永久保存
         */
        private long ttl = 0;

        /**
         * 单位毫秒
         * 最长空闲时间
         * 默认永久保存
         */
        private long maxIdleTime = 0;


        /**
         * maxSize
         */
        private int maxSize = 0;
    }
}
