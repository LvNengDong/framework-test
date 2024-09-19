package cn.lnd.ibatis.type;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 16:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappedTypes {
    Class<?>[] value();
}
