package org.xiaowu.behappy.redis.annotation;

import java.lang.annotation.*;

/**
 * 当使用Kryo序列化时，需要bean上标注此注解
 * @author xiaowu
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface KryoSerialize {
}
