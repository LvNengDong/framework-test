package cn.lnd.ibatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/18 17:04
 */
public interface Cache {

    /**
     * @return The identifier of this cache
     *
     * 返回此缓存对象的唯一标识
     */
    String getId();

    /**
     * 添加指定键的值
     */
    void putObject(Object key, Object value);

    /**
     * 获得指定键的值
     */
    Object getObject(Object key);

    /**
     * 移除指定键的值
     *
     * @param key The key
     * @return Not used
     */
    Object removeObject(Object key);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 获得容器中缓存的数量
     */
    int getSize();

    /**
     * 获得读取写锁。该方法可以忽略了已经。
     *
     * Optional. As of 3.2.6 this method is no longer called by the core.
     *
     * Any locking needed by the cache must be provided internally by the cache provider.
     *
     * @return A ReadWriteLock
     */
    @Deprecated // add by 芋艿
    ReadWriteLock getReadWriteLock();

}
