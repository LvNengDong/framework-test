package cn.lnd.ibatis.annotations;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description 查询语句注解
 * @Date 2024/9/21 01:20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) // 方法
public @interface Select {
    /**
     * @return 查询语句
     */
    String[] value();
}
