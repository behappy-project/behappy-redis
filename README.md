## 技术沟通群
<img src="https://raw.githubusercontent.com/wang-xiaowu/picture_repository/master/behappy_group.jpg" width="300px">

## BeHappy Redis

* 集成redisson
* 写了些redis常用轮子,避免重复造
* 配置简单,使用方便
* 支持springboot3.x

## 使用方式
### pom
```
<dependency>
    <groupId>io.github.wang-xiaowu</groupId>
    <artifactId>behappy-redis</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 配置

- redisson.yaml配置, 详细请[参见redisson配置方式](https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95), [redisson-spring-boot-starter](https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter)

```
# 例: 单节点设置
singleServerConfig:
  # （节点地址）redis://-非ssl,rediss://-ssl
  address: redis://127.0.0.1:6379
  database: 0
  password: null
  # 如果当前连接池里的连接数量超过了`最小空闲连接数`，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒。
  idleConnectionTimeout: 10000
  # 同节点建立连接时的等待超时。时间单位是毫秒。
  connectTimeout: 10000
  # 等待节点回复命令的时间。该时间从命令发送成功时开始计时。
  timeout: 3000
  # 如果尝试达到 retryAttempts（命令失败重试次数） 仍然不能将命令发送至某个指定的节点时，将抛出错误。如果尝试在此限制之内发送成功，则开始启用 timeout（命令等待超时） 计时。
  retryAttempts: 3
  # 在某个节点执行相同或不同命令时，连续 失败 failedAttempts（执行失败最大次数） 时，该节点将被从可用节点列表里清除，直到 reconnectionTimeout（重新连接时间间隔） 超时以后再次尝试。
  retryInterval: 1500
  # 在Redis节点里显示的客户端名称。
  clientName: null
  # 发布和订阅连接的最小空闲连接数 默认1
  subscriptionConnectionMinimumIdleSize: 1
  # 发布和订阅连接池大小 默认50
  subscriptionConnectionPoolSize: 50
  # 单个连接最大订阅数量 默认5
  subscriptionsPerConnection: 5
  # 最小空闲连接数 默认32，现在暂时不需要那么多的线程
  connectionMinimumIdleSize: 4
  # 连接池大小,在启用该功能以后，Redisson将会监测DNS的变化情况。
  connectionPoolSize: 64
  # 监测DNS的变化情况的时间间隔。
  dnsMonitoringInterval: 5000
# 这个线程池数量被所有RTopic对象监听器，RRemoteService调用者和RExecutorService任务共同共享。
threads: 0
# 这个线程池数量是在一个Redisson实例内，被其创建的所有分布式数据类型和服务，以及底层客户端所一同共享的线程池里保存的线程数量。
nettyThreads: 0
# (如果不配置则默认为Kryo5Codec方式)，当前支持JsonJacksonCodec和Kryo5Codec序列化方式
codec: 
  class: org.redisson.codec.JsonJacksonCodec
transportMode: "NIO"
```

- application.yaml

```
behappy:
  redis:
    # 如果使用Kryo5Codec序列化方式，需要注册对应bean；配置如下属性，程序启动时会扫描对应包下包含@KryoSerialize的类进行注册
    register-clazz-packages: 
      - xxx.xxx.xxx
    # banner打印
    banner-shown: false
    # CacheManager缓存配置
    cache-manager:
      # 缓存cache 前缀
      prefix: BEHAPPY
      configs:
          # cacheName
        - key: test
          # 过期时间
          ttl: 0
    # 配置http session（默认关闭）
    session-enable: true
    session-config:
      cookie-max-age: 1
      cookie-name: GULIMALL
# 参考org.redisson.spring.starter.RedissonAutoConfiguration
spring:
  redis:
    redisson:
      file: classpath:redisson.yaml
```

## 限流

```
@RateLimit
/**
 * 限流唯一标示, 随limitType变化,默认为ip
 * @return
 */
String key() default "";

/**
 * 单位时间
 * @return
 */
long time() default 5;

/**
 * 产生令牌数
 * @return
 */
long count() default 1;

/**
 * 限制类型（ip/方法名）
 */
LimitTypeEnum limitType() default LimitTypeEnum.IP;

/**
 * @apiNote
 * RRateLimiter 速度类型
 * OVERALL,    所有客户端加总限流
 * PER_CLIENT; 每个客户端单独计算流量
 * @return
 */
RateType mode() default RateType.PER_CLIENT;
```

## 分布式锁

```
@Lock(key = "#name")
/**
 * 锁的key,支持spel表达式
 */
String key();
/**
 * 获取锁的最大尝试时间(单位 {@code unit})
 * 该值大于0则使用 locker.tryLock 方法加锁，否则使用 locker.lock 方法
 */
long waitTime() default 0;
/**
 * 加锁的时间(单位 {@code unit})，超过这个时间后锁便自动解锁；
 * 如果leaseTime为-1，则保持锁定直到显式解锁
 */
long leaseTime() default -1;
/**
 * 参数的时间单位
 */
TimeUnit unit() default TimeUnit.SECONDS;
/**
 * 是否公平锁
 */
boolean isFair() default false;
```

## 幂等

```
@Idempotent
/**
 * 幂等操作的唯一标识，使用spring el表达式 用#来引用方法参数
 */
String key() default "";

/**
 * 有效期 默认：1 有效期要大于程序执行时间，否则请求还是可能会进来
 * @return expireTime
 */
int expireTime() default 1;

/**
 * 时间单位 默认：s
 * @return TimeUnit
 */
TimeUnit timeUnit() default TimeUnit.SECONDS;

/**
 * 是否在业务完成后删除key true:删除 false:不删除
 * 如果不删除,则是变向的限流
 * @return boolean
 */
boolean delKey() default false;
```
