package org.xiaowu.behappy.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private RedisSerializer<Object> redisSerializer() {
        if (redisson.getConfig().getCodec() instanceof org.redisson.codec.Kryo5Codec) {
            return new KryoRedisSerializer<>(beHappyRedisProperties.getRegisterClazzPackages());
        }
        //创建JSON序列化器
        return new Jackson2JsonRedisSerializer<>(getObjectMapper(), Object.class);
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

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 日期序列化设置
        objectMapper.registerModule(new JavaTimeModule());
        // 简单类型序列化配置
        objectMapper.registerModule((new SimpleModule()));
        //禁用注解支持，防止一些@ignore的字段被忽略
        objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        //指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会抛出异常(如果不标注此属性，解析将是一个LinkHashMap类型的key-value的数据结构)
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return objectMapper;
    }
}
