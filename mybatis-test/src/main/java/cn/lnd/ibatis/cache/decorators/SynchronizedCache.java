package cn.lnd.ibatis.cache.decorators;

import cn.lnd.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/18 21:51
 */
public class SynchronizedCache implements Cache {

    private Cache delegate;

    public SynchronizedCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public synchronized int getSize() {
        return delegate.getSize();
    }

    @Override
    public synchronized void putObject(Object key, Object object) {
        delegate.putObject(key, object);
    }

    @Override
    public synchronized Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public synchronized Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public synchronized void clear() {
        delegate.clear();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

}
