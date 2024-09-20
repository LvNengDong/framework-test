package cn.lnd.ibatis.annotations;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Lang {
    Class<?> value();
}
