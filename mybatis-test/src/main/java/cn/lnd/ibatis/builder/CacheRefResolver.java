package cn.lnd.ibatis.builder;

import cn.lnd.ibatis.cache.Cache;

/**
 * @Author lnd
 * @Description Cache 指向解析器
 *
 *      CacheRefResolver 中记录了被引用的 namespace以及当前 namespace 关联的MapperBuilderAssistant 对象。
 *      前面在解析 <cache>标签的时候我们介绍过，MapperBuilderAssistant 会在 useNewCache() 方法中通过 CacheBuilder 创建新的 Cache 对象，并记录到 currentCache 字段。
 *      而这里解析 <cache-ref> 标签的时候，MapperBuilderAssistant 会通过 useCacheRef() 方法从 Configuration.caches 集合中，根据被引用的namespace 查找共享的 Cache 对象来初始化 currentCache，而不再创建新的Cache 对象，从而实现二级缓存的共享。
 *
 * @Date 2024/9/19 15:20
 */
public class CacheRefResolver {
    /* 当前 namespace 关联的MapperBuilderAssistant 对象 */
    private final MapperBuilderAssistant assistant;
    /* Cache 引用的命名空间 */
    private final String cacheRefNamespace;

    public CacheRefResolver(MapperBuilderAssistant assistant, String cacheRefNamespace) {
        this.assistant = assistant;
        this.cacheRefNamespace = cacheRefNamespace;
    }

    /**
     * 获得指向的 Cache 对象
     * */
    public Cache resolveCacheRef() {
        return assistant.useCacheRef(cacheRefNamespace);
    }
}
