package cn.lnd.ibatis.cache.impl;


import cn.lnd.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @Author lnd
 * @Description 实现 Cache 接口，永不过期的 Cache 实现类，基于 HashMap 实现
 *
 * @Date 2024/9/18 17:06
 */
public class PerpetualCache implements Cache {

    // 标识
    private String id;

    // 缓存容器
    private Map<Object, Object> cache = new HashMap<Object, Object>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getSize() {
        return cache.size();
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cache)) {
            return false;
        }

        Cache otherCache = (Cache) o;
        return getId().equals(otherCache.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) {
            throw new CacheException("Cache instances require an ID.");
        }
        return getId().hashCode();
    }

}

/*
    delegate 属性，被装饰的 Cache 对象。

    在 #getObject(Object key) 方法，增加了 requests 和 hits 的计数，从而实现命中比率的统计，即 #getHitRatio() 方法。
 */