package org.xiaowu.behappy.redis.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xiaowu.behappy.redis.annotation.Idempotent;
import org.xiaowu.behappy.redis.exception.BeHappyException;
import org.xiaowu.behappy.redis.util.CommonUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.xiaowu.behappy.redis.metadata.BizCodeEnum.REPEATED_REQUESTS;
import static org.xiaowu.behappy.redis.metadata.Constant.IDEMPOTENT_KEY;

/**
 * @author xiaowu
 * 幂等切面
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class IdempotentAspect {

    // 创建threadLocal<map>
    private static final ThreadLocal<Map<String, Object>> THREAD_CACHE = ThreadLocal.withInitial(HashMap::new);

    private static final String KEY = "key";

    private static final String DELKEY = "delKey";

    private final RedissonClient redisson;

    @Pointcut("@annotation(org.xiaowu.behappy.redis.annotation.Idempotent)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void beforePointCut(JoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (!method.isAnnotationPresent(Idempotent.class)) {
            return;
        }
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        String key;
        // 若没有配置 幂等 标识编号，则使用 url + 参数列表作为区分
        if (!StringUtils.hasLength(idempotent.key())) {
            String url = request.getRequestURL().toString();
            String argString = Arrays.asList(joinPoint.getArgs()).toString();
            key = url + argString;
        }else if (idempotent.key().contains("#")){
            // 使用jstl 规则区分
            key = CommonUtils.getValBySpEL(idempotent.key(), signature, joinPoint.getArgs());
        }else {
            key = idempotent.key();
        }
        // 当配置了el表达式但是所选字段为空时,会抛出异常,兜底使用url做标识
        if(key == null){
            key = request.getRequestURL().toString();
        }
        long expireTime = idempotent.expireTime();
        TimeUnit timeUnit = idempotent.timeUnit();
        boolean delKey = idempotent.delKey();

        RMapCache<String, Object> rMapCache = redisson.getMapCache(IDEMPOTENT_KEY);
        String value = LocalDateTime.now().toString().replace("T", " ");
        // 如果不等于空则说明正有线程操作当前key
        if (null != rMapCache.get(key)) {
            throw new BeHappyException(REPEATED_REQUESTS.getCode(),REPEATED_REQUESTS.getMsg());
        }
        Object v1 = rMapCache.putIfAbsent(key, value, expireTime, timeUnit);
        // 二次验证
        if (null != v1) {
            throw new BeHappyException(REPEATED_REQUESTS.getCode(),REPEATED_REQUESTS.getMsg());
        }else {
            log.info("[idempotent]:has stored key={},value={},expireTime={}{},now={}", key, value, expireTime,
                    timeUnit, LocalDateTime.now());
        }

        Map<String, Object> map = THREAD_CACHE.get();
        map.put(KEY, key);
        map.put(DELKEY, delKey);

    }

    /**
     * 判断是否需要移除当前操作的key
     * @param joinPoint
     */
    @After("pointCut()")
    public void afterPointCut(JoinPoint joinPoint){
        Map<String, Object> threadCacheMap = THREAD_CACHE.get();
        if (CollectionUtils.isEmpty(threadCacheMap)){
            return;
        }
        RMapCache<Object, Object> mapCache = redisson.getMapCache(IDEMPOTENT_KEY);
        if (mapCache.size() == 0){
            return;
        }
        String key = threadCacheMap.get(KEY).toString();
        boolean delKey = (boolean) threadCacheMap.get(DELKEY);
        if (delKey){
            mapCache.fastRemove(key);
        }
        THREAD_CACHE.remove();
    }
}
