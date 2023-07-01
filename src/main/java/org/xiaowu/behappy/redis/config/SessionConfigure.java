package org.xiaowu.behappy.redis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.AbstractRedisHttpSessionConfiguration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.xiaowu.behappy.redis.util.CommonUtils;






/**
 * @author xiaowu
 * @Description: springSession配置类
 **/

@Configuration
@RequiredArgsConstructor
@EnableRedisHttpSession
@ConditionalOnProperty(prefix = "behappy.redis", name = "session-enable", havingValue = "true")
@EnableConfigurationProperties({BeHappySessionProperties.class})
public class SessionConfigure {

    private final BeHappySessionProperties beHappySessionProperties;

    @Bean
    @ConditionalOnMissingBean
    public CookieSerializer cookieSerializer() {
        BeHappySessionProperties.SessionConfig sessionConfig = beHappySessionProperties.getSessionConfig();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        if (StringUtils.hasLength(sessionConfig.getCookieName())) {
            cookieSerializer.setCookieName(sessionConfig.getCookieName());
        }
        if (!ObjectUtils.isEmpty(sessionConfig.getUseSecureCookie())) {
            cookieSerializer.setUseSecureCookie(sessionConfig.getUseSecureCookie());
        }
        cookieSerializer.setUseHttpOnlyCookie(sessionConfig.isUseHttpOnlyCookie());
        if (!ObjectUtils.isEmpty(sessionConfig.getUseSecureCookie())) {
            cookieSerializer.setUseSecureCookie(sessionConfig.getUseSecureCookie());
        }
        if (StringUtils.hasLength(sessionConfig.getCookiePath())) {
            cookieSerializer.setCookiePath(sessionConfig.getCookiePath());
        }
        if (!ObjectUtils.isEmpty(sessionConfig.getCookieMaxAge())) {
            cookieSerializer.setCookieMaxAge(sessionConfig.getCookieMaxAge());
        }
        // 放大作用域, 设置cookie的作用域为父域名
        if (StringUtils.hasLength(sessionConfig.getDomainName())) {
            cookieSerializer.setDomainName(sessionConfig.getDomainName());
        }
        if (StringUtils.hasLength(sessionConfig.getDomainNamePattern())) {
            cookieSerializer.setDomainNamePattern(sessionConfig.getDomainNamePattern());
        }
        if (StringUtils.hasLength(sessionConfig.getJvmRoute())) {
            cookieSerializer.setJvmRoute(sessionConfig.getJvmRoute());
        }
        cookieSerializer.setUseBase64Encoding(sessionConfig.isUseBase64Encoding());
        if (StringUtils.hasLength(sessionConfig.getRememberMeRequestAttribute())) {
            cookieSerializer.setRememberMeRequestAttribute(sessionConfig.getRememberMeRequestAttribute());
        }
        if (StringUtils.hasLength(sessionConfig.getSameSite())) {
            cookieSerializer.setSameSite(sessionConfig.getSameSite());
        }
        return cookieSerializer;
    }

    /**
     * @see AbstractRedisHttpSessionConfiguration#setDefaultRedisSerializer
     * @return
     */
    @Bean("springSessionDefaultRedisSerializer")
    @ConditionalOnMissingBean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new Jackson2JsonRedisSerializer<>(CommonUtils.getObjectMapper(), Object.class);
    }

}
