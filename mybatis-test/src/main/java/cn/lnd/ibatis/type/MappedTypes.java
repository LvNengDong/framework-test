package cn.lnd.ibatis.type;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 *      作用：用于指定Java对象与数据库类型的映射关系。
 *          该注解可以用于JavaBean属性或ResultMap中，用于指定Java类型对应的JDBC类型。
 * @Date 2024/9/19 16:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 注册到类
public @interface MappedTypes {
    /**
     * @return 匹配的 Java Type 类型的数组
     */
    Class<?>[] value();
}
