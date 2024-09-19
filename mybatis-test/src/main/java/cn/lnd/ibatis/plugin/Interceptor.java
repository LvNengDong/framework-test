package cn.lnd.ibatis.plugin;

import org.apache.ibatis.plugin.Invocation;

import java.util.Properties;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 00:13
 */
public interface Interceptor {

    Object intercept(Invocation invocation) throws Throwable;

    Object plugin(Object target);

    void setProperties(Properties properties);

}
