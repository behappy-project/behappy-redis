package org.xiaowu.behappy.redis.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ClassUtils;
import org.xiaowu.behappy.redis.annotation.KryoSerialize;

import java.io.IOException;
import java.util.*;

/**
 * 公共工具类
 * @author xiaowu
 */
@Slf4j
@UtilityClass
public class CommonUtils {

    /**
     * 用于SpEL表达式解析.
     */
    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    private static final String RESOURCE_PATTERN = "/**/*.class";

    /**
     * 用于获取方法参数定义名字.
     */
    private static final DefaultParameterNameDiscoverer DEFAULT_PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();


    /**
     * checkNullDefaultVal
     * @param obj
     * @param defaultVal
     * @return
     */
    public Object checkNullDefaultVal(Object obj, Object defaultVal) {
        boolean nullFlag = Objects.isNull(obj);
        if (nullFlag){
            return defaultVal;
        }
        return obj;
    }


    /**
     * 解析spEL表达式
     */
    public String getValBySpEL(String spEL, MethodSignature methodSignature, Object[] args) {
        //获取方法形参名数组
        String[] paramNames = DEFAULT_PARAMETER_NAME_DISCOVERER.getParameterNames(methodSignature.getMethod());
        if (paramNames != null && paramNames.length > 0) {
            Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(spEL);
            // spring的表达式上下文对象
            EvaluationContext context = new StandardEvaluationContext();
            // 给上下文赋值
            for(int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            return expression.getValue(context).toString();
        }
        return null;
    }

    public List<Class<?>> scanClazz(List<String> basePackages) {
        List<Class<?>> classes = new ArrayList<>();
        // spring工具类，可以获取指定路径下的全部类
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            for (String basePackage : basePackages) {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(basePackage) + RESOURCE_PATTERN;
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                //MetadataReader 的工厂类
                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                for (Resource resource : resources) {
                    //用于读取类信息
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    //扫描到的class
                    String classname = reader.getClassMetadata().getClassName();
                    Class<?> clazz = Class.forName(classname);
                    // 判断是否有指定主解
                    KryoSerialize anno = clazz.getAnnotation(KryoSerialize.class);
                    if (anno != null) {
                        classes.add(clazz);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("redis扫描注册类出现错误： {}",e.getMessage());
        }
        return classes;
    }
}
