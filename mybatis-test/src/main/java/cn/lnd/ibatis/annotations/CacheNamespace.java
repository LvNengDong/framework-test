package cn.lnd.ibatis.annotations;

import cn.lnd.ibatis.annotations.Property;
import cn.lnd.ibatis.cache.decorators.LruCache;
import cn.lnd.ibatis.cache.impl.PerpetualCache;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheNamespace {
    Class<? extends cn.lnd.ibatis.cache.Cache> implementation() default PerpetualCache.class;

    Class<? extends cn.lnd.ibatis.cache.Cache> eviction() default LruCache.class;

    long flushInterval() default 0;

    int size() default 1024;

    boolean readWrite() default true;

    boolean blocking() default false;

    /**
     * Property values for a implementation object.
     * @since 3.4.2
     */
    Property[] properties() default {};

}