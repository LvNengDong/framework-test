package cn.lnd.ibatis.type;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description 匹配的 JDBC Type 类型的注解
 * @Date 2024/9/19 16:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 注册到类
public @interface MappedJdbcTypes {

    /**
     * @return 匹配的 JDBC Type 类型的注解
     */
    JdbcType[] value();

    /**
     * @return 是否包含 {@link java.sql.JDBCType#NULL}
     */
    boolean includeNullJdbcType() default false;

}
