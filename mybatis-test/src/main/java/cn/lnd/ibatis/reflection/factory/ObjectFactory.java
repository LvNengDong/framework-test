package cn.lnd.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;
/**
 * @Author lnd
 * @Description Object 工厂接口，用于创建指定类的对象。
 * @Date 2024/9/6 21:43
 */
public interface ObjectFactory {
    /**
     * 设置 Properties
     * @param properties configuration properties
     */
    void setProperties(Properties properties);

    /**
     * 创建指定类的对象，使用默认构造方法
     * @param type Object type
     * @return 对象
     */
    <T> T create(Class<T> type);

    /**
     * 创建指定类的对象，使用特定的构造方法
     *
     * @param type Object type
     * @param constructorArgTypes Constructor argument types 指定构造方法的参数列表
     * @param constructorArgs Constructor argument values 参数数组
     * @return 对象
     */
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

    /**
     * 判断指定类是否为集合类
     *
     * @param type Object type
     */
    <T> boolean isCollection(Class<T> type);
}
