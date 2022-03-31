package org.xiaowu.behappy.redis.serializer;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTDecoder;
import org.nustaq.serialization.FSTEncoder;
import org.nustaq.serialization.coders.FSTStreamDecoder;
import org.nustaq.serialization.coders.FSTStreamEncoder;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * https://blog.51cto.com/u_15064630/4541604
 * 使用fts进行序列化
 * 10倍于JDK序列化性能而且100%兼容的编码
 */
public class FstSerializer {
    // 单例的生成工厂,
    // 并且让FSTStreamDecoder.chBufS FSTStreamDecoder.ascStringCache为空
    static class FSTDefaultStreamCoderFactory implements FSTConfiguration.StreamCoderFactory {

        Field chBufField;
        Field ascStringCacheField;

        {
            try {
                chBufField = FSTStreamDecoder.class.getDeclaredField("chBufS");
                ascStringCacheField = FSTStreamDecoder.class.getDeclaredField("ascStringCache");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            ascStringCacheField.setAccessible(true);
            chBufField.setAccessible(true);
        }

        private FSTConfiguration fstConfiguration;

        FSTDefaultStreamCoderFactory(FSTConfiguration fstConfiguration) {
            this.fstConfiguration = fstConfiguration;
        }

        @Override
        public FSTEncoder createStreamEncoder() {
            return new FSTStreamEncoder(fstConfiguration);
        }

        @Override
        public FSTDecoder createStreamDecoder() {
            return new FSTStreamDecoder(fstConfiguration) {
                @Override
                public String readStringUTF() throws IOException {
                    try {
                        String res = super.readStringUTF();
                        // xField.set(FSTStreamDecoder obj, Object value) 把FSTStreamDecoder对象的xField设置为value
                        chBufField.set(this, null);
                        return res;
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                }

                @Override
                public String readStringAsc() throws IOException {
                    try {
                        String res = super.readStringAsc();
                        ascStringCacheField.set(this, null);
                        return res;
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                }
            };
        }

        static ThreadLocal input = new ThreadLocal();
        static ThreadLocal output = new ThreadLocal();

        @Override
        public ThreadLocal getInput() {
            return input;
        }

        @Override
        public ThreadLocal getOutput() {
            return output;
        }

    }

    private static class InstanceHolder {
        private static final FSTConfiguration INSTANCE = FSTConfiguration.createDefaultConfiguration();
        static {
            INSTANCE.setStreamCoderFactory(new FSTDefaultStreamCoderFactory(INSTANCE));
        }
    }

    public FSTConfiguration getConfig() {
        return InstanceHolder.INSTANCE;
    }
}
