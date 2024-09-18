package cn.lnd.ibatis.cache.decorators;


import cn.lnd.ibatis.cache.Cache;
import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * @Author lnd
 * @Description 实现 Cache 接口，支持打印日志的 Cache 实现类
 * @Date 2024/9/18 17:10
 */
public class LoggingCache implements Cache {

    // MyBatis Log 对象
    private Log log;
    // 装饰的 Cache 对象
    private Cache delegate;
    // 统计请求缓存的次数
    protected int requests = 0;
    // 统计命中缓存的次数
    protected int hits = 0;

    public LoggingCache(Cache delegate) {
        this.delegate = delegate;
        this.log = LogFactory.getLog(getId());
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public void putObject(Object key, Object object) {
        delegate.putObject(key, object);
    }

    @Override
    public Object getObject(Object key) {
        // 请求次数 ++
        requests++;
        // 获得缓存
        final Object value = delegate.getObject(key);
        // 如果命中缓存，则命中次数 ++
        if (value != null) {
            hits++;
        }
        if (log.isDebugEnabled()) {
            log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
        }
        return value;
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    private double getHitRatio() {
        return (double) hits / (double) requests;
    }

}
