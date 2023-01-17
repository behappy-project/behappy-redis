package org.xiaowu.behappy.redis.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.util.StringUtils;
import org.xiaowu.behappy.redis.annotation.Lock;
import org.xiaowu.behappy.redis.exception.BeHappyException;
import org.xiaowu.behappy.redis.service.RedissonLockService;
import org.xiaowu.behappy.redis.util.CommonUtils;

import static org.xiaowu.behappy.redis.metadata.BizCodeEnum.LOCK_WAIT_TIMEOUT;

/**
 * @author xiaowu
 * 分布式锁切面
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class LockAspect {

    private final RedissonLockService locker;

    /**
     * "@within(lock) "自定义注解标注在的类上；该类的所有方法（不包含子类方法）执行aop方法
     * "@annotation(lock)" 自定义注解标注在方法上的方法执行aop方法
     * @param point
     * @param lock
     * @return
     * @throws Throwable
     */
    @Around("@within(lock) || @annotation(lock)")
    public Object aroundLock(ProceedingJoinPoint point, Lock lock) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)point.getSignature();
        if (lock == null) {
            // 获取类上的注解
            lock = point.getTarget().getClass().getDeclaredAnnotation(Lock.class);
        }
        String lockKey = lock.key();
        // 默认方法名为key
        if (!StringUtils.hasLength(lockKey)) {
            lockKey = methodSignature.getMethod().getName();
        }else if (lockKey.contains("#")) {
            //获取方法参数值
            Object[] args = point.getArgs();
            lockKey = CommonUtils.getValBySpEL(lockKey, methodSignature, args);
        }
        // 当配置了el表达式但是所选字段为空时,会抛出异常,兜底使用方法名做标识
        if(lockKey == null){
            lockKey = methodSignature.getMethod().getName();
        }
        RLock rLock = null;
        try {
            //加锁
            if (lock.waitTime() > 0) {
                rLock = locker.tryLock(lockKey, lock.waitTime(), lock.leaseTime(), lock.unit(), lock.isFair());
            } else {
                rLock = locker.lock(lockKey, lock.leaseTime(), lock.unit(), lock.isFair());
            }
            if (rLock != null) {
                return point.proceed();
            } else {
                throw new BeHappyException(LOCK_WAIT_TIMEOUT.getCode(),LOCK_WAIT_TIMEOUT.getMsg());
            }
        } finally {
            // 释放锁
            locker.unlock(rLock);
        }
    }

}
