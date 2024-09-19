package cn.lnd.ibatis.reflection;

import cn.lnd.ibatis.reflection.factory.DefaultObjectFactory;
import cn.lnd.ibatis.reflection.factory.ObjectFactory;
import cn.lnd.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.lnd.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 00:06
 */
public final class SystemMetaObject {

    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    public static final cn.lnd.ibatis.reflection.MetaObject NULL_META_OBJECT = cn.lnd.ibatis.reflection.MetaObject.forObject(cn.lnd.ibatis.reflection.SystemMetaObject.NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());

    private SystemMetaObject() {
        // Prevent Instantiation of Static Class
    }

    private static class NullObject {
    }

    public static cn.lnd.ibatis.reflection.MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
    }

}
