package cn.lnd.ibatis.executor.loader.javassist;

import cn.lnd.ibatis.executor.loader.AbstractSerialStateHolder;
import cn.lnd.ibatis.executor.loader.ResultLoaderMap;
import cn.lnd.ibatis.reflection.factory.ObjectFactory;

import java.util.List;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 17:39
 */
class JavassistSerialStateHolder extends AbstractSerialStateHolder {

    private static final long serialVersionUID = 8940388717901644661L;

    public JavassistSerialStateHolder() {
    }

    public JavassistSerialStateHolder(
            final Object userBean,
            final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
            final ObjectFactory objectFactory,
            List<Class<?>> constructorArgTypes,
            List<Object> constructorArgs) {
        super(userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
    }

    @Override
    protected Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory,
                                                List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        return new JavassistProxyFactory().createDeserializationProxy(target, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
    }
}
