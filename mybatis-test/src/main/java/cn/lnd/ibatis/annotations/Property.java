package cn.lnd.ibatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Property {
    /**
     * A target property name
     */
    String name();
    /**
     * A property value or placeholder
     */
    String value();
}
