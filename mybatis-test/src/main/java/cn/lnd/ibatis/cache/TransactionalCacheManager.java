package cn.lnd.ibatis.cache;

import cn.lnd.ibatis.cache.Cache;
import cn.lnd.ibatis.cache.CacheKey;
import cn.lnd.ibatis.cache.decorators.TransactionalCache;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:34
 */
public class TransactionalCacheManager {

    private Map<cn.lnd.ibatis.cache.Cache, TransactionalCache> transactionalCaches = new HashMap<cn.lnd.ibatis.cache.Cache, TransactionalCache>();

    public void clear(cn.lnd.ibatis.cache.Cache cache) {
        getTransactionalCache(cache).clear();
    }

    public Object getObject(cn.lnd.ibatis.cache.Cache cache, cn.lnd.ibatis.cache.CacheKey key) {
        return getTransactionalCache(cache).getObject(key);
    }

    public void putObject(cn.lnd.ibatis.cache.Cache cache, CacheKey key, Object value) {
        getTransactionalCache(cache).putObject(key, value);
    }

    public void commit() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.commit();
        }
    }

    public void rollback() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.rollback();
        }
    }

    private TransactionalCache getTransactionalCache(Cache cache) {
        TransactionalCache txCache = transactionalCaches.get(cache);
        if (txCache == null) {
            txCache = new TransactionalCache(cache);
            transactionalCaches.put(cache, txCache);
        }
        return txCache;
    }

}
