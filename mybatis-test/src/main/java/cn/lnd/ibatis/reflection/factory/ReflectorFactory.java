package cn.lnd.ibatis.reflection.factory;

import cn.lnd.ibatis.reflection.Reflector;

/**
 * @Author lnd
 * @Description Reflector 工厂接口，用于创建和缓存 Reflector 对象。
 * @Date 2024/9/6 21:31
 */
public interface ReflectorFactory {
    /**
     * @return 是否缓存 Reflector 对象
     */
    boolean isClassCacheEnabled();

    /**
     * 设置是否缓存 Reflector 对象
     *
     * @param classCacheEnabled 是否缓存
     */
    void setClassCacheEnabled(boolean classCacheEnabled);

    /**
     * 获取 Reflector 对象
     *
     * @param type 指定类
     * @return Reflector 对象
     */
    Reflector findForClass(Class<?> type);
}
