package cn.lnd.ibatis.type;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 15:22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Alias {
    String value();
}
