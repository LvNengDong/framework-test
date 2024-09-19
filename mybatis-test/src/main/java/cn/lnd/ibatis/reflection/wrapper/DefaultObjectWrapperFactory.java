package cn.lnd.ibatis.reflection.wrapper;

import cn.lnd.ibatis.reflection.MetaObject;
import cn.lnd.ibatis.reflection.ReflectionException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 00:09
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory {

    @Override
    public boolean hasWrapperFor(Object object) {
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }

}
