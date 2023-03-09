package org.xiaowu.behappy.redis.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xiaowu.behappy.redis.register.User;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author xiaowu
 */
@Service
public class TestService {
    @Cacheable(cacheNames = "BEHAPPY:DICT_CACHE", key = "#id")
    public List<User> findUsers(Long id) {
        List<User> users = new ArrayList<>();
        users.add(new User("小五",27));
        return users;
    }

    @Cacheable(cacheNames = "BEHAPPY:DICT_CACHE", key = "#id")
    public User findUser(Long id) {
        return new User("小五",27);
    }
}
