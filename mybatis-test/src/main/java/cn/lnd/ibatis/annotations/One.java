package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.mapping.FetchType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface One {
    String select() default "";

    FetchType fetchType() default FetchType.DEFAULT;

}
