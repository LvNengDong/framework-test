package cn.lnd.ibatis.reflection.wrapper;

import cn.lnd.ibatis.reflection.MetaObject;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 00:09
 */
public interface ObjectWrapperFactory {

    boolean hasWrapperFor(Object object);

    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);

}
