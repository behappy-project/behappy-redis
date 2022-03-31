package org.xiaowu.behappy.redis.annotation;

import org.redisson.api.RateType;
import org.xiaowu.behappy.redis.metadata.LimitTypeEnum;

import java.lang.annotation.*;

/**
 * @author xiaowu
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 限流唯一标示
     * @return
     */
    String key() default "";

    /**
     * 单位时间
     * @return
     */
    long time() default 5;

    /**
     * 产生令牌数
     * @return
     */
    long count() default 1;

    /**
     * 限制类型（ip/方法名）
     */
    LimitTypeEnum limitType() default LimitTypeEnum.IP;

    /**
     * @apiNote
     * RRateLimiter 速度类型
     * OVERALL,    所有客户端加总限流
     * PER_CLIENT; 每个客户端单独计算流量
     * @return
     */
    RateType mode() default RateType.PER_CLIENT;
}