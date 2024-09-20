package cn.lnd.ibatis.binding;

import cn.lnd.ibatis.builder.annotation.MapperAnnotationBuilder;
import cn.lnd.ibatis.io.ResolverUtil;
import cn.lnd.ibatis.session.Configuration;
import cn.lnd.ibatis.session.SqlSession;

import java.util.*;

/**
 * @Author lnd
 * @Description Mapper 注册表
 * @Date 2024/9/19 11:24
 */
public class MapperRegistry {

    // MyBatis Configuration 对象
    private final Configuration config;

    /*
    * MapperProxyFactory 的映射
    *   KEY：Mapper 接口
    * */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();


    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    /**
     * 获得 Mapper Proxy 对象
     *
     */
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        // <1> 获得 MapperProxyFactory 对象
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        // 不存在，则抛出 BindingException 异常
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        // 创建 Mapper Proxy 对象
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    /**
     * 判断 knownMappers 是否有 Mapper
     * */
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        // <1> 判断 type 必须是接口，也就是说 Mapper 接口
        if (type.isInterface()) {
            // <2> 已经添加过，则抛出 BindingException 异常
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                // <3> 添加到 knownMappers 中
                knownMappers.put(type, new MapperProxyFactory<>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                // <4> 解析 Mapper 的注解配置
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                // <5> 标记加载完成
                loadCompleted = true;
            } finally {
                // <6> 若加载未完成，从 knownMappers 中移除
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }

    /**
     * @since 3.2.2
     */
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }

    /**
     * 扫描指定包，并将符合条件的类，添加到 knownMappers 中
     *
     * @since 3.2.2
     */
    public void addMappers(String packageName, Class<?> superType) {
        // <1> 扫描指定包下的满足条件的类们
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses(); // 获得结果集
        // <2> 遍历，添加到 knownMappers 中
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    /**
     * @since 3.2.2
     */
    public void addMappers(String packageName) {
        addMappers(packageName, Object.class);
    }

}
