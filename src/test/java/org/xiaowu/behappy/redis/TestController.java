package org.xiaowu.behappy.redis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaowu.behappy.redis.annotation.Idempotent;
import org.xiaowu.behappy.redis.annotation.Lock;

/**
 * TestController
 * @author xiaowu
 */
@RestController
public class TestController {

    private static int a = 100;

    @GetMapping("/idempotent")
    @Idempotent(key = "#key", expireTime = 3)
    public String idempotent(@RequestParam String key) throws Exception {
        Thread.sleep(2000L);
        return "success";
    }

    @GetMapping("/lock")
    @Lock
    public String lock() throws Exception {
        a--;
        System.out.println(a);
        return "success";
    }
}
