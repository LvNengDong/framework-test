package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.type.JdbcType;
import cn.lnd.ibatis.type.TypeHandler;
import cn.lnd.ibatis.type.UnknownTypeHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Arg {
    boolean id() default false;

    String column() default "";

    Class<?> javaType() default void.class;

    JdbcType jdbcType() default JdbcType.UNDEFINED;

    Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

    String select() default "";

    String resultMap() default "";
}
