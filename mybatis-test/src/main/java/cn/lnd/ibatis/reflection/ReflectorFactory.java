package cn.lnd.ibatis.reflection;


/**
 * @Author lnd
 * @Description
 * @Date 2024/9/11 16:55
 */
public interface ReflectorFactory {
    boolean isClassCacheEnabled();

    void setClassCacheEnabled(boolean classCacheEnabled);

    Reflector findForClass(Class<?> type);
}
