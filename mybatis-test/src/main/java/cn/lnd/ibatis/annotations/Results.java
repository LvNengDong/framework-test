package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.annotations.Result;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Results {
    /**
     * The name of the result map.
     */
    String id() default "";
    Result[] value() default {};
}
