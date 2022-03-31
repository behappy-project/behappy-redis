package org.xiaowu.behappy.redis.metadata;
 
/**
 * @apiNote  限流类型
 */
public enum LimitTypeEnum {
    /**
     * 自定义类型
     */
    CUSTOMER,
 
    /**
     *  根据 IP地址限制
     */
    IP
}