package cn.lnd.ibatis.builder;

import cn.lnd.ibatis.builder.MapperBuilderAssistant;
import cn.lnd.ibatis.cache.Cache;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 15:20
 */
public class CacheRefResolver {
    private final MapperBuilderAssistant assistant;
    private final String cacheRefNamespace;

    public CacheRefResolver(MapperBuilderAssistant assistant, String cacheRefNamespace) {
        this.assistant = assistant;
        this.cacheRefNamespace = cacheRefNamespace;
    }

    public Cache resolveCacheRef() {
        return assistant.useCacheRef(cacheRefNamespace);
    }
}
