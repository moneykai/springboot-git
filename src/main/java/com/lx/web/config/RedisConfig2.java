package com.lx.web.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * @ClassName RedisConfig
 * @Description springboot2 redis配置类
 * @Author zhuwenbin
 * @Date 2019/2/16 11:36
 */
@Configuration
@EnableCaching
public class RedisConfig2 extends CachingConfigurerSupport {

    @Value(value = "spring.application.name")
    private String applicationName;

    /**
     * 选择redis作为默认缓存工具
     *
     * @param factory
     * @return
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory).build();
        return redisCacheManager;
    }

    /**
     * redisTemplate配置
     *
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(factory);

        //使用FastJson序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        FastJsonRedisSerializer<Object> jsonRedisSerializer = new FastJsonRedisSerializer<Object>(Object.class);
        // 全局开启AutoType，不建议使用
        // ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        // 建议使用这种方式，小范围指定白名单
        ParserConfig.getGlobalInstance().addAccept("com.lx.springboot.entity");

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        /*Jackson2JsonRedisSerializer jsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jsonRedisSerializer.setObjectMapper(om);*/

        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        // String值采用json序列化
        template.setValueSerializer(jsonRedisSerializer);

        // 设置hash key 和value序列化模式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * key生成规则
     *
     * 注意: 该方法只是声明了key的生成策略,还未被使用,需在@Cacheable注解中指定keyGenerator
     *       如: @Cacheable(value = "key", keyGenerator = "cacheKeyGenerator")
     *
     * @return
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                //应用名:类名:方法名:参数列表
                sb.append(applicationName).append(":");
                sb.append(target.getClass().getName()).append(":");
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(":").append(obj.toString());
                }
                return sb.toString();
            }
        };
    }

    /**
     * FastJson序列化器
     *
     * 解决Jackson2JsonRedisSerializer序列化时必须指定序列化的对象类型否则反序列化失败的问题
     *
     * 注意: redisTemplate应该使用自定义的FastJsonRedisSerializer而不是FastJson包中的FastJsonRedisSerializer
     *       否则无法反序列化
     *
      * @param <T>
     */
    class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

        public final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

        private Class<T> clazz;

        public FastJsonRedisSerializer(Class<T> clazz) {
            super();
            this.clazz = clazz;
        }

        @Override
        public byte[] serialize(T t) throws SerializationException {
            if (t == null) {
                return new byte[0];
            }
            return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
        }

        @Override
        public T deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length <= 0) {
                return null;
            }
            String str = new String(bytes, DEFAULT_CHARSET);
            return JSON.parseObject(str, clazz);
        }

    }

    /**
     * StringRedisSerializer序列化器
     *
     * 解决@Cacheable注解的key会报类型转换错误
     *
     */
    class StringRedisSerializer implements RedisSerializer<Object> {

        private final Charset charset;

        private final String target = "\"";

        private final String replacement = "";

        public StringRedisSerializer() {
            this(Charset.forName("UTF8"));
        }

        public StringRedisSerializer(Charset charset) {
            Assert.notNull(charset, "Charset must not be null!");
            this.charset = charset;
        }

        @Override
        public String deserialize(byte[] bytes) {
            return (bytes == null ? null : new String(bytes, charset));
        }

        @Override
        public byte[] serialize(Object object) {
            String string = JSON.toJSONString(object);
            if (string == null) {
                return null;
            }
            string = string.replace(target, replacement);
            return string.getBytes(charset);
        }
    }

}
