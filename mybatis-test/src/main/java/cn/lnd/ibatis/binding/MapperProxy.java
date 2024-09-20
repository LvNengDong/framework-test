package cn.lnd.ibatis.binding;

import cn.lnd.ibatis.lang.UsesJava7;
import cn.lnd.ibatis.reflection.ExceptionUtil;
import cn.lnd.ibatis.session.SqlSession;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @Author lnd
 * @Description 实现 InvocationHandler、Serializable 接口，Mapper Proxy 。关键是 java.lang.reflect.InvocationHandler 接口，你懂的。
 * @Date 2024/9/19 15:06
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -6424540398559729838L;

    /** SqlSession 对象 */
    private final SqlSession sqlSession;

    /** Mapper 接口 */
    private final Class<T> mapperInterface;

    /**
     * 方法与 MapperMethod 的映射
     * 从 {@link MapperProxyFactory#methodCache} 传递过来
     * */
    private final Map<Method, MapperMethod> methodCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    /**
     * 代理方法
     * */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // <1> 如果是 Object 定义的方法，直接调用
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            // 见 https://github.com/mybatis/mybatis-3/issues/709 ，支持 JDK8 default 方法
            else if (isDefaultMethod(method)) {
                /* JDK8 在接口上，新增了 default 修饰符。怎么进行反射调用，见 《java8 通过反射执行接口的default方法》 一文。（https://www.jianshu.com/p/63691220f81f） */
                return invokeDefaultMethod(proxy, method, args);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
        // <3.1> 获得 MapperMethod 对象
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        // <3.2> 执行 MapperMethod 方法
        return mapperMethod.execute(sqlSession, args);
    }

    /**
     * 获得 MapperMethod 对象
     * 默认从 methodCache 缓存中获取。如果不存在，则进行创建，并进行缓存。
     * */
    private MapperMethod cachedMapperMethod(Method method) {
        MapperMethod mapperMethod = methodCache.get(method);
        if (mapperMethod == null) {
            mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
            methodCache.put(method, mapperMethod);
        }
        return mapperMethod;
    }

    @UsesJava7
    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
            throws Throwable {
        final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }

    /**
     * 判断是否为 default 修饰的方法
     */
    private boolean isDefaultMethod(Method method) {
        return ((method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }
}
