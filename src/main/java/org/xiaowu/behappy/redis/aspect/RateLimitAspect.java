package org.xiaowu.behappy.redis.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.*;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xiaowu.behappy.redis.annotation.RateLimit;
import org.xiaowu.behappy.redis.exception.BeHappyException;
import org.xiaowu.behappy.redis.metadata.LimitTypeEnum;
import org.xiaowu.behappy.redis.util.IPUtils;

import java.lang.reflect.Method;
import java.util.Objects;

import static org.xiaowu.behappy.redis.metadata.BizCodeEnum.BEYOND_THE_FREQUENCY_LIMIT;

/**
 * @author xiaowu
 * 限流切面
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint,RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = servletRequestAttributes.getRequest();
        StringBuffer ipBuffer = new StringBuffer();
        String url = request.getRequestURI();
        ipBuffer.append(StringUtils.replace(url, "/", "_"));
        ipBuffer.append("_");
        //获取请求IP信息
        ipBuffer.append(IPUtils.getClientIp(request));
        //获取配置RRateLimiter信息
        RRateLimiter rRateLimiter = getRRateLimiter(joinPoint, ipBuffer.toString(),rateLimit);
        boolean flag = rRateLimiter.tryAcquire();
        if (!flag){
            log.error("IP【{}】访问唯一标识【{}】超出频率限制，限制规则为[限流模式：{}; 限流数量：{}; 限流时间间隔：{};]",
                    ipBuffer, rateLimit.key(), rateLimit.mode().toString(), rateLimit.count(), rateLimit.time());
            throw new BeHappyException(BEYOND_THE_FREQUENCY_LIMIT.getCode(),BEYOND_THE_FREQUENCY_LIMIT.getMsg());
        }
        return joinPoint.proceed();
    }

    /**
     * @param joinPoint
     * @param ip
     * @return
     */
    private RRateLimiter getRRateLimiter(ProceedingJoinPoint joinPoint, String ip,RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String key;
        if (Objects.equals(rateLimit.limitType(), LimitTypeEnum.IP)) {
            key = ip;
        } else if (Objects.equals(rateLimit.limitType(), LimitTypeEnum.CUSTOMER)) {
            key = rateLimit.key();
        } else {
            //默认采用方法名
            key = method.getName().toLowerCase();
        }
        long count = rateLimit.count();//限流次数
        long time = rateLimit.time();//限流时间
        RateType mode = rateLimit.mode();//限流类型
        RRateLimiter rRateLimiter = redissonClient.getRateLimiter(key);
        if (rRateLimiter.isExists()) {
            RateLimiterConfig rateLimiterConfig = rRateLimiter.getConfig();//读取已经存在配置
            long rateInterval = rateLimiterConfig.getRateInterval()/1000;//限时时间
            long rate = rateLimiterConfig.getRate();//次数
            RateType rateType = rateLimiterConfig.getRateType();//类型
            if (time != rateInterval || rate != count || mode != rateType) {
                //移除配置，重新加载配置
                rRateLimiter.delete();
                // 最大流速 = 每${time}秒钟产生${count}个令牌
                rRateLimiter.trySetRate(mode, count, time, RateIntervalUnit.SECONDS);
            }
        }else {
            // 最大流速 = 每${time}秒钟产生${count}个令牌
            rRateLimiter.trySetRate(mode, count, time, RateIntervalUnit.SECONDS);
        }
        return rRateLimiter;
    }
}
