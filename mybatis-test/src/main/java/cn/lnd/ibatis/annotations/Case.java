package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.annotations.Arg;
import cn.lnd.ibatis.annotations.Result;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Case {
    String value();

    Class<?> type();

    Result[] results() default {};

    Arg[] constructArgs() default {};
}
