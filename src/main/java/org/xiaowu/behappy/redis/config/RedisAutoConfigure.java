package org.xiaowu.behappy.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;
import org.xiaowu.behappy.redis.repository.CacheManagerRepository;
import org.xiaowu.behappy.redis.repository.RedisRepository;
import org.xiaowu.behappy.redis.serializer.FstCodec;
import org.xiaowu.behappy.redis.serializer.FstRedisSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * redis ?????????
 * @author xiaowu
 */
@EnableCaching
@AllArgsConstructor
@AutoConfigureBefore(RedissonAutoConfiguration.class)
@EnableConfigurationProperties({RedisProperties.class, BeHappyRedisProperties.class})
public class RedisAutoConfigure {

    private final BeHappyRedisProperties beHappyRedisProperties;

    private final RedissonClient redisson;

    @Bean
    public CacheManagerRepository cacheManagerRepository(@Autowired CacheManager cacheManager) {
        return new CacheManagerRepository(cacheManager);
    }

    @Bean
    public RedisRepository redisRepository(@Autowired RedisTemplate redisTemplate) {
        return new RedisRepository(redisTemplate);
    }

    @Bean
    public RedisSerializer<String> redisKeySerializer() {
        return RedisSerializer.string();
    }

    @Bean
    @DependsOn("redisson")
    public RedisSerializer<Object> redisValueSerializer() {
        if (redisson.getConfig().getCodec() instanceof FstCodec) {
            return new FstRedisSerializer();
        }
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(getObjectMapper());
        return genericJackson2JsonRedisSerializer;
    }

    /**
     * StringRedisTemplate??????
     * @param factory
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory
            , RedisSerializer<String> redisKeySerializer, RedisSerializer<Object> redisValueSerializer) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setDefaultSerializer(redisValueSerializer);
        redisTemplate.setKeySerializer(redisKeySerializer);
        redisTemplate.setHashKeySerializer(redisKeySerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * RedisTemplate??????
     * @param factory
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory
            , RedisSerializer<String> redisKeySerializer, RedisSerializer<Object> redisValueSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setDefaultSerializer(redisValueSerializer);
        redisTemplate.setKeySerializer(redisKeySerializer);
        redisTemplate.setHashKeySerializer(redisKeySerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @DependsOn("redisson")
    public CacheManager cacheManager(RedissonClient redisson) {
        //????????????????????????????????????
        Map<String, CacheConfig> redisCacheConfigurationMap = new HashMap<String, CacheConfig>(16);
        if (!CollectionUtils.isEmpty(beHappyRedisProperties.getCacheManager().getConfigs())) {
            beHappyRedisProperties.getCacheManager().getConfigs().forEach(e -> {
                String cacheName = beHappyRedisProperties.getCacheManager().getPrefix().concat(":").concat(e.getKey()).concat(":");
                CacheConfig cacheConfig = new CacheConfig(e.getTtl() * 60 * 1000, e.getMaxIdleTime() * 60 * 1000);
                cacheConfig.setMaxSize(e.getMaxSize());
                redisCacheConfigurationMap.put(cacheName, cacheConfig);
            });
        }
        return new RedissonSpringCacheManager(redisson, redisCacheConfigurationMap);
    }

    /**
     * ?????????key????????????(???:??????)
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(":" + method.getName() + ":");
            for (Object obj : objects) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }


    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // ?????????????????????
        objectMapper.registerModule(new JavaTimeModule());
        // ???????????????????????????
        objectMapper.registerModule((new SimpleModule()));
        //?????????????????????????????????@ignore??????????????????
        objectMapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        // ???????????????????????????field,get???set,????????????????????????ANY???????????????private???public
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // ????????????????????????????????????????????????final????????????final?????????????????????String,Integer??????????????????
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        return objectMapper;
    }
}
