package org.xiaowu.behappy.redis.config;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import org.xiaowu.behappy.redis.serializer.KryoRedisSerializer;
import org.xiaowu.behappy.redis.util.CommonUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * redis 配置类
 * @author xiaowu
 */
@EnableCaching
@Configuration
@RequiredArgsConstructor
@AutoConfigureBefore(RedissonAutoConfiguration.class)
@EnableConfigurationProperties({RedisProperties.class, BeHappyRedisProperties.class})
public class RedisAutoConfigure {

    private final BeHappyRedisProperties beHappyRedisProperties;

    private final RedissonClient redisson;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(redisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(redisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
        //设置Redis缓存有效期为1天
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                // 默认缓存有效期
                //.entryTtl(Duration.ofDays(1L))
                // key值双冒号处理
                .computePrefixWith(name -> name + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer()));
        // 对每个缓存空间应用不同的配置
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(beHappyRedisProperties.getCacheManager().getConfigs())) {
            beHappyRedisProperties.getCacheManager().getConfigs().forEach(e -> {
                String cacheName = beHappyRedisProperties.getCacheManager().getPrefix().concat(":").concat(e.getKey());
                configMap.put(cacheName, defaultCacheConfig.entryTtl(Duration.ofSeconds(e.getTtl())));
            });
        }
        return RedisCacheManager.builder(redisCacheWriter)
                .cacheDefaults(defaultCacheConfig)
                .initialCacheNames(configMap.keySet())
                .withInitialCacheConfigurations(configMap)
                .transactionAware()
                .build();
    }

    private RedisSerializer<Object> redisSerializer() {
        if (redisson.getConfig().getCodec() instanceof org.redisson.codec.Kryo5Codec) {
            return new KryoRedisSerializer<>(beHappyRedisProperties.getRegisterClazzPackages());
        }
        //创建JSON序列化器
        return new Jackson2JsonRedisSerializer<>(CommonUtils.getObjectMapper(), Object.class);
    }
}
