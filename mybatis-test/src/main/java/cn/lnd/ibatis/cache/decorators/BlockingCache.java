package cn.lnd.ibatis.cache.decorators;

import cn.lnd.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author lnd
 * @Description  实现 Cache 接口，阻塞的 Cache 实现类
 *
 *      这里的阻塞比较特殊，当线程去获取缓存值时，如果不存在，则会阻塞后续的其他线程去获取该缓存。
 *      为什么这么有这样的设计呢？因为当线程 A 在获取不到缓存值时，一般会去设置对应的缓存值，这样就避免其他也需要该缓存的线程 B、C 等，重复添加缓存。
 *
 * @Date 2024/9/18 21:28
 */
public class BlockingCache implements Cache {

    // 阻塞等待超时时间
    private long timeout;

    // 被装饰的 Cache 对象
    private final Cache delegate;

    // 缓存键与 ReentrantLock 锁对象的映射
    private final ConcurrentHashMap<Object, ReentrantLock> locks;

    public BlockingCache(Cache delegate) {
        this.delegate = delegate;
        this.locks = new ConcurrentHashMap<>();
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
    public void putObject(Object key, Object value) {
        try {
            // <2.1> 添加缓存
            delegate.putObject(key, value);
        } finally {
            // <2.2> 释放锁
            releaseLock(key);
        }
    }

    /**
     *
     * <1.1> 处，首先会获得锁。这样其它线程来获取该值时，将被阻塞等待。那岂不是有问题？答案在 <1.3> 处。
     * <1.2> 处，然后，获得缓存值。
     * <1.3> 处，获得缓存值成功时，会释放锁，这样被阻塞等待的其他线程就可以去获取缓存了。但是，如果获得缓存值失败时，就需要在 #putObject(Object key, Object value) 方法中，添加缓存时，才会释放锁，这样被阻塞等待的其它线程就不会重复添加缓存了。
     * @param key
     * @return
     */
    @Override
    public Object getObject(Object key) {
        // <1.1> 获得锁
        acquireLock(key);
        // <1.2> 获得缓存值
        Object value = delegate.getObject(key);
        // <1.3> 释放锁
        if (value != null) {
            releaseLock(key);
        }
        return value;
    }

    /**
     * 它很特殊，和方法名字有所“冲突”，不会移除对应的缓存，只会移除锁。
     *
     * @param key The key
     * @return
     */
    @Override
    public Object removeObject(Object key) {
        // despite of its name, this method is called only to release locks
        // 释放锁
        releaseLock(key);
        return null;
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /**
     * 获得 ReentrantLock 锁对象
     *
     * @param key 缓存键
     * @return ReentrantLock 对象
     */
    private ReentrantLock getLockForKey(Object key) {
        ReentrantLock lock = new ReentrantLock();
        ReentrantLock previous = locks.putIfAbsent(key, lock); // 检查指定的键是否已经存在于 ConcurrentHashMap 中。如果键不存在，则将指定的键值对添加到 ConcurrentHashMap 中，并返回 null。如果键已经存在，则不会进行任何操作，并返回与该键关联的当前值。
        return previous == null ? lock : previous;
    }


    /**
     * 获得指定 key 对象的 ReentrantLock 锁对象
     * @param key
     */
    private void acquireLock(Object key) {
        // 获得 ReentrantLock 锁对象。
        Lock lock = getLockForKey(key);
        // 获得锁，直到超时
        if (timeout > 0) {
            try {
                boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
                // acquired 为 true 表示获取到锁，如果等待超时仍未获取到锁，返回 false
                if (!acquired) {
                    throw new CacheException("Couldn't get a lock in " + timeout + " for the key " +  key + " at the cache " + delegate.getId());
                }
            } catch (InterruptedException e) {
                throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
            }
        } else {
            // 如果超时，释放锁
            lock.lock();
        }
    }

    private void releaseLock(Object key) {
        // 获得 key 对应的 ReentrantLock 锁对象
        ReentrantLock lock = locks.get(key);
        // 如果当前线程持有，进行释放
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
