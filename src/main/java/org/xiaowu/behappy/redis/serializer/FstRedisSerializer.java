package org.xiaowu.behappy.redis.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

/**
 * 10倍于JDK序列化性能而且100%兼容的编码
 * 使用fst 进行Fst的序列化
 */
public class FstRedisSerializer implements RedisSerializer<Object> {

    private static final byte[] EMPTY_ARRAY = new byte[0];

    @Override
    @SneakyThrows
    public byte[] serialize(Object o) {
        if (o == null) {
            return EMPTY_ARRAY;
        }
        return new FstSerializer().getConfig().asByteArray(o);
    }

    @Override
    @SneakyThrows
    public Object deserialize(byte[] bytes) {
        if (isEmpty(bytes)) {
            return null;
        }
        return new FstSerializer().getConfig().asObject(bytes);
    }

    private static boolean isEmpty(@Nullable byte[] data) {
        return (data == null || data.length == 0);
    }
}