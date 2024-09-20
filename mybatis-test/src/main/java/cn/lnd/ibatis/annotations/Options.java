package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.mapping.ResultSetType;
import cn.lnd.ibatis.mapping.StatementType;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/21 01:18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
    /**
     * The options for the {@link cn.lnd.ibatis.annotations.Options#flushCache()}.
     * The default is {@link cn.lnd.ibatis.annotations.Options.FlushCachePolicy#DEFAULT}
     */
    public enum FlushCachePolicy {
        /** <code>false</code> for select statement; <code>true</code> for insert/update/delete statement. */
        DEFAULT,
        /** Flushes cache regardless of the statement type. */
        TRUE,
        /** Does not flush cache regardless of the statement type. */
        FALSE
    }

    boolean useCache() default true;

    cn.lnd.ibatis.annotations.Options.FlushCachePolicy flushCache() default cn.lnd.ibatis.annotations.Options.FlushCachePolicy.DEFAULT;

    ResultSetType resultSetType() default ResultSetType.FORWARD_ONLY;

    StatementType statementType() default StatementType.PREPARED;

    int fetchSize() default -1;

    int timeout() default -1;

    boolean useGeneratedKeys() default false;

    String keyProperty() default "id";

    String keyColumn() default "";

    String resultSets() default "";
}
