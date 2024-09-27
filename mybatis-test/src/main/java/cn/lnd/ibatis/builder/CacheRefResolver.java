package cn.lnd.ibatis.builder;

import cn.lnd.ibatis.cache.Cache;

/**
 * @Author lnd
 * @Description Cache 指向解析器
 * @Date 2024/9/19 15:20
 */
public class CacheRefResolver {
    private final MapperBuilderAssistant assistant;
    /* Cache 指向的命名空间 */
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
