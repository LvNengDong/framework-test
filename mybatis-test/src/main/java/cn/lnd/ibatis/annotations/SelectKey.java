package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.mapping.StatementType;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectKey {
    String[] statement();

    String keyProperty();

    String keyColumn() default "";

    boolean before();

    Class<?> resultType();

    StatementType statementType() default StatementType.PREPARED;
}
