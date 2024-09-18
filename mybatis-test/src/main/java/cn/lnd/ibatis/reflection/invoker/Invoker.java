package cn.lnd.ibatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/4 18:09
 */
public interface Invoker {
    Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

    Class<?> getType();
}
