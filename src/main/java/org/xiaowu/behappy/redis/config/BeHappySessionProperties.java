package org.xiaowu.behappy.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Http Session 配置属性
 * @author xiaowu
 */
@Data
@ConfigurationProperties(prefix = "behappy.redis")
public class BeHappySessionProperties {

    /**
     * @see SessionConfig
     * SessionConfig配置
     */
    private SessionConfig sessionConfig = new SessionConfig();

    @Data
    static class SessionConfig {

        private String cookieName = "SESSION";

        private Boolean useSecureCookie;

        private boolean useHttpOnlyCookie = true;

        private String cookiePath;

        private Integer cookieMaxAge;

        private String domainName;

        private String domainNamePattern;

        private String jvmRoute;

        private boolean useBase64Encoding = true;

        private String rememberMeRequestAttribute;

        private String sameSite = "Lax";
    }
}
