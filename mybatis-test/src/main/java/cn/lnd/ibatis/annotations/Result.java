package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.annotations.Many;
import cn.lnd.ibatis.annotations.One;
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
 * @Date 2024/9/21 01:19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Result {
    boolean id() default false;

    String column() default "";

    String property() default "";

    Class<?> javaType() default void.class;

    JdbcType jdbcType() default JdbcType.UNDEFINED;

    Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

    cn.lnd.ibatis.annotations.One one() default @One;

    cn.lnd.ibatis.annotations.Many many() default @Many;
}
