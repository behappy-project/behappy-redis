package org.xiaowu.behappy.redis.metadata;

/**
 * constant
 * @author xiaowu
 */
public interface Constant {
    String LOCK_KEY_PREFIX = "LOCK_KEY";

    /**
     * 配置banner选项,true/false
     */
    String BANNER_SHOWN = "behappy.redis.banner-shown";

    String CACHE_PREFIX = "BEHAPPY";

    String IDEMPOTENT_KEY = "idempotent";

}
