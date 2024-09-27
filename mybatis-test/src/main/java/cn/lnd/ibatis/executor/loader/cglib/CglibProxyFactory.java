package cn.lnd.ibatis.executor.loader.cglib;

import cn.lnd.ibatis.executor.loader.*;
import cn.lnd.ibatis.executor.loader.cglib.CglibSerialStateHolder;
import cn.lnd.ibatis.io.Resources;
import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;
import cn.lnd.ibatis.reflection.ExceptionUtil;
import cn.lnd.ibatis.reflection.factory.ObjectFactory;
import cn.lnd.ibatis.reflection.property.PropertyCopier;
import cn.lnd.ibatis.reflection.property.PropertyNamer;
import cn.lnd.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 17:38
 */
public class CglibProxyFactory implements ProxyFactory {

    private static final String FINALIZE_METHOD = "finalize";
    private static final String WRITE_REPLACE_METHOD = "writeReplace";

    public CglibProxyFactory() {
        try {
            Resources.classForName("net.sf.cglib.proxy.Enhancer");
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot enable lazy loading because CGLIB is not available. Add CGLIB to your classpath.", e);
        }
    }

    @Override
    public Object createProxy(Object target, cn.lnd.ibatis.executor.loader.ResultLoaderMap lazyLoader, cn.lnd.ibatis.session.Configuration configuration, cn.lnd.ibatis.reflection.factory.ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        return cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.EnhancedResultObjectProxyImpl.createProxy(target, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
    }

    public Object createDeserializationProxy(Object target, Map<String, cn.lnd.ibatis.executor.loader.ResultLoaderMap.LoadPair> unloadedProperties, cn.lnd.ibatis.reflection.factory.ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        return cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.EnhancedDeserializationProxyImpl.createProxy(target, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
    }

    @Override
    public void setProperties(Properties properties) {
        // Not Implemented
    }

    static Object crateProxy(Class<?> type, Callback callback, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(callback);
        enhancer.setSuperclass(type);
        try {
            type.getDeclaredMethod(WRITE_REPLACE_METHOD);
            // ObjectOutputStream will call writeReplace of objects returned by writeReplace
            if (cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.LogHolder.log.isDebugEnabled()) {
                cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.LogHolder.log.debug(WRITE_REPLACE_METHOD + " method was found on bean " + type + ", make sure it returns this");
            }
        } catch (NoSuchMethodException e) {
            enhancer.setInterfaces(new Class[]{cn.lnd.ibatis.executor.loader.WriteReplaceInterface.class});
        } catch (SecurityException e) {
            // nothing to do here
        }
        Object enhanced;
        if (constructorArgTypes.isEmpty()) {
            enhanced = enhancer.create();
        } else {
            Class<?>[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
            Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
            enhanced = enhancer.create(typesArray, valuesArray);
        }
        return enhanced;
    }

    private static class EnhancedResultObjectProxyImpl implements MethodInterceptor {

        private final Class<?> type;
        private final cn.lnd.ibatis.executor.loader.ResultLoaderMap lazyLoader;
        private final boolean aggressive;
        private final Set<String> lazyLoadTriggerMethods;
        private final cn.lnd.ibatis.reflection.factory.ObjectFactory objectFactory;
        private final List<Class<?>> constructorArgTypes;
        private final List<Object> constructorArgs;

        private EnhancedResultObjectProxyImpl(Class<?> type, cn.lnd.ibatis.executor.loader.ResultLoaderMap lazyLoader, cn.lnd.ibatis.session.Configuration configuration, cn.lnd.ibatis.reflection.factory.ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
            this.type = type;
            this.lazyLoader = lazyLoader;
            this.aggressive = configuration.isAggressiveLazyLoading();
            this.lazyLoadTriggerMethods = configuration.getLazyLoadTriggerMethods();
            this.objectFactory = objectFactory;
            this.constructorArgTypes = constructorArgTypes;
            this.constructorArgs = constructorArgs;
        }

        public static Object createProxy(Object target, cn.lnd.ibatis.executor.loader.ResultLoaderMap lazyLoader, Configuration configuration, cn.lnd.ibatis.reflection.factory.ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
            final Class<?> type = target.getClass();
            cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.EnhancedResultObjectProxyImpl callback = new cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.EnhancedResultObjectProxyImpl(type, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
            Object enhanced = crateProxy(type, callback, constructorArgTypes, constructorArgs);
            cn.lnd.ibatis.reflection.property.PropertyCopier.copyBeanProperties(type, target, enhanced);
            return enhanced;
        }

        @Override
        public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            final String methodName = method.getName();
            try {
                synchronized (lazyLoader) {
                    if (WRITE_REPLACE_METHOD.equals(methodName)) {
                        Object original;
                        if (constructorArgTypes.isEmpty()) {
                            original = objectFactory.create(type);
                        } else {
                            original = objectFactory.create(type, constructorArgTypes, constructorArgs);
                        }
                        cn.lnd.ibatis.reflection.property.PropertyCopier.copyBeanProperties(type, enhanced, original);
                        if (lazyLoader.size() > 0) {
                            return new cn.lnd.ibatis.executor.loader.cglib.CglibSerialStateHolder(original, lazyLoader.getProperties(), objectFactory, constructorArgTypes, constructorArgs);
                        } else {
                            return original;
                        }
                    } else {
                        if (lazyLoader.size() > 0 && !FINALIZE_METHOD.equals(methodName)) {
                            if (aggressive || lazyLoadTriggerMethods.contains(methodName)) {
                                lazyLoader.loadAll();
                            } else if (cn.lnd.ibatis.reflection.property.PropertyNamer.isSetter(methodName)) {
                                final String property = cn.lnd.ibatis.reflection.property.PropertyNamer.methodToProperty(methodName);
                                lazyLoader.remove(property);
                            } else if (cn.lnd.ibatis.reflection.property.PropertyNamer.isGetter(methodName)) {
                                final String property = PropertyNamer.methodToProperty(methodName);
                                if (lazyLoader.hasLoader(property)) {
                                    lazyLoader.load(property);
                                }
                            }
                        }
                    }
                }
                return methodProxy.invokeSuper(enhanced, args);
            } catch (Throwable t) {
                throw ExceptionUtil.unwrapThrowable(t);
            }
        }
    }

    private static class EnhancedDeserializationProxyImpl extends cn.lnd.ibatis.executor.loader.AbstractEnhancedDeserializationProxy implements MethodInterceptor {

        private EnhancedDeserializationProxyImpl(Class<?> type, Map<String, cn.lnd.ibatis.executor.loader.ResultLoaderMap.LoadPair> unloadedProperties, cn.lnd.ibatis.reflection.factory.ObjectFactory objectFactory,
                                                 List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
            super(type, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
        }

        public static Object createProxy(Object target, Map<String, cn.lnd.ibatis.executor.loader.ResultLoaderMap.LoadPair> unloadedProperties, cn.lnd.ibatis.reflection.factory.ObjectFactory objectFactory,
                                         List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
            final Class<?> type = target.getClass();
            cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.EnhancedDeserializationProxyImpl callback = new cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.EnhancedDeserializationProxyImpl(type, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
            Object enhanced = crateProxy(type, callback, constructorArgTypes, constructorArgs);
            PropertyCopier.copyBeanProperties(type, target, enhanced);
            return enhanced;
        }

        @Override
        public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            final Object o = super.invoke(enhanced, method, args);
            return o instanceof cn.lnd.ibatis.executor.loader.AbstractSerialStateHolder ? o : methodProxy.invokeSuper(o, args);
        }

        @Override
        protected cn.lnd.ibatis.executor.loader.AbstractSerialStateHolder newSerialStateHolder(Object userBean, Map<String, cn.lnd.ibatis.executor.loader.ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory,
                                                                                                   List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
            return new cn.lnd.ibatis.executor.loader.cglib.CglibSerialStateHolder(userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
        }
    }

    private static class LogHolder {
        private static final Log log = LogFactory.getLog(cn.lnd.ibatis.executor.loader.cglib.CglibProxyFactory.class);
    }

}
