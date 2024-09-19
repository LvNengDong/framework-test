package cn.lnd.ibatis.executor.loader;

import cn.lnd.ibatis.executor.loader.ResultLoaderMap;
import cn.lnd.ibatis.reflection.factory.ObjectFactory;
import cn.lnd.ibatis.session.Configuration;

import java.util.List;
import java.util.Properties;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 00:01
 */
public interface ProxyFactory {

    void setProperties(Properties properties);

    Object createProxy(Object target, ResultLoaderMap lazyLoader, Configuration configuration, ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

}
