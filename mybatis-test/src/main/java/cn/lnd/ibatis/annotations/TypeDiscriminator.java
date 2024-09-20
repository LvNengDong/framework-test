package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.annotations.Case;
import cn.lnd.ibatis.type.JdbcType;
import cn.lnd.ibatis.type.TypeHandler;
import cn.lnd.ibatis.type.UnknownTypeHandler;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:21
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TypeDiscriminator {
    String column();

    Class<?> javaType() default void.class;

    JdbcType jdbcType() default JdbcType.UNDEFINED;

    Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

    Case[] cases();
}
