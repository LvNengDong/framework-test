package cn.lnd.ibatis.reflection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author lnd
 * @Description 默认的 ReflectorFactory 实现类
 * @Date 2024/9/6 21:32
 */
public class DefaultReflectorFactory implements ReflectorFactory {
    /**
     * 是否缓存
     */
    private boolean classCacheEnabled = true;

    /**
     * Reflector 的缓存映射
     *
     * KEY：类
     * VALUE：Reflector 对象
     */
    private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

    public DefaultReflectorFactory() {
    }

    @Override
    public boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    @Override
    public void setClassCacheEnabled(boolean classCacheEnabled) {
        this.classCacheEnabled = classCacheEnabled;
    }

    @Override
    public Reflector findForClass(Class<?> type) {
        // 开启缓存，则从 reflectorMap 中获取
        if (classCacheEnabled) {
            // synchronized (type) removed see issue #461
            return reflectorMap.computeIfAbsent(type, Reflector::new); // 不存在，则进行创建
            // 关闭缓存，则创建 Reflector 对象
        } else {
            return new Reflector(type);
        }
    }
}
