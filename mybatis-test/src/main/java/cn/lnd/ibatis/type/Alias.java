package cn.lnd.ibatis.type;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description 别名的注解
 * @Date 2024/9/19 15:22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Alias {
    /**
     * @return 别名
     */
    String value();
}
