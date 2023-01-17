package org.xiaowu.behappy.redis;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.xiaowu.behappy.redis.register.User;

/**
 * TestApplication
 * @author xiaowu
 */
@SpringBootTest
@SpringBootApplication(scanBasePackages = "org.xiaowu.behappy.redis")
public class TestApplication {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    RedissonClient redisson;

    MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @SneakyThrows
    @Test
    public void testIdempotent() {
        mockMvc.perform(MockMvcRequestBuilders.get("/idempotent?key=1")).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andReturn();
    }

    @SneakyThrows
    @Test
    @RepeatedTest(100)
    public void severalThreadTestLock() {
        mockMvc.perform(MockMvcRequestBuilders.get("/lock")).
                andExpect(MockMvcResultMatchers.status().isOk()).
                andReturn();
    }


    @Test
    void testRedis() {
        User user = new User("小米", 12);
        redisTemplate.opsForValue().set("test", user);
        System.out.println(redisTemplate.opsForValue().get("test"));
    }



}
