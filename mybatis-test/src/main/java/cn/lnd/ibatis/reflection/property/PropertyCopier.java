package cn.lnd.ibatis.reflection.property;

import java.lang.reflect.Field;

/**
 * @Author lnd
 * @Description
 *  PropertyCopier 是一个属性拷贝的工具类，提供了与 Spring 中 BeanUtils.copyProperties() 类似的功能，
 *  实现相同类型的两个对象之间的属性值拷贝，其核心方法是 copyBeanProperties() 方法。
 * @Date 2024/9/9 22:42
 */
public final class PropertyCopier {

    private PropertyCopier() {
        // Prevent Instantiation of Static Class
    }

    /**
     * 将 sourceBean 的属性，复制到 destinationBean 中
     *
     * @param type 指定类
     * @param sourceBean 来源Bean对象
     * @param destinationBean 目标Bean对象
     */
    public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
        // 循环，从当前类开始，不断复制父类，直到父类不存在
        Class<?> parent = type;
        while (parent != null) {
            // 获取parent类定义的属性
            final Field[] fields = parent.getDeclaredFields();
            for(Field field : fields) {
                try {
                    // 设置属性可访问
                    field.setAccessible(true);
                    // 从 sourceBean 中，复制到 destinationBean 去
                    field.set(destinationBean, field.get(sourceBean));
                } catch (Exception e) {
                    // Nothing useful to do, will only fail on final fields, which will be ignored.
                }
            }
            // 获取父类
            parent = parent.getSuperclass();
        }
    }

}
