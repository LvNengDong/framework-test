package cn.lnd.ibatis.lang;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 15:07
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
public @interface UsesJava8 {
}
