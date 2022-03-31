package org.xiaowu.behappy.redis.util;

import lombok.experimental.UtilityClass;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Objects;

/**
 * 公共工具类
 * @author xiaowu
 */
@UtilityClass
public class CommonUtils {

    /**
     * 用于SpEL表达式解析.
     */
    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

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
}
