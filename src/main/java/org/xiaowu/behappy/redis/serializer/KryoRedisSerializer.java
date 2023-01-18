/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package org.xiaowu.behappy.redis.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.redisson.codec.KryoCodec;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.xiaowu.behappy.redis.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

/**
 * 使用Kryo 进行reids的序列化
 * @author LGH
 */
@Slf4j
public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    private final KryoCodec kryoPool;
    private final List<String> basePackages;

    public KryoRedisSerializer(List<String> basePackages) {
        kryoPool = new KryoCodec(Collections.emptyList(), null);
        this.basePackages = basePackages;
    }

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];


    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return EMPTY_BYTE_ARRAY;
        }

        Kryo kryo = kryoPool.get();
        registerClazz(kryo);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            kryo.writeClassAndObject(output, t);
            output.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return EMPTY_BYTE_ARRAY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        Kryo kryo = kryoPool.get();
        registerClazz(kryo);

        try (Input input = new Input(bytes)) {
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void registerClazz(Kryo kryo) {
        List<Class<?>> classes = CommonUtils.scanClazz(basePackages);
        for (Class<?> clazz : classes) {
            kryo.register(clazz);
        }
    }
}
