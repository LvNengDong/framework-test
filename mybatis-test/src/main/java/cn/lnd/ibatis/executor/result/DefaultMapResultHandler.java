package cn.lnd.ibatis.executor.result;

import cn.lnd.ibatis.reflection.MetaObject;
import cn.lnd.ibatis.reflection.ReflectorFactory;
import cn.lnd.ibatis.reflection.factory.ObjectFactory;
import cn.lnd.ibatis.reflection.wrapper.ObjectWrapperFactory;
import cn.lnd.ibatis.session.ResultContext;
import cn.lnd.ibatis.session.ResultHandler;

import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 20:48
 */
public class DefaultMapResultHandler<K, V> implements ResultHandler<V> {

    private final Map<K, V> mappedResults;
    private final String mapKey;
    private final ObjectFactory objectFactory;
    private final ObjectWrapperFactory objectWrapperFactory;
    private final ReflectorFactory reflectorFactory;

    @SuppressWarnings("unchecked")
    public DefaultMapResultHandler(String mapKey, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;
        this.reflectorFactory = reflectorFactory;
        this.mappedResults = objectFactory.create(Map.class);
        this.mapKey = mapKey;
    }

    @Override
    public void handleResult(ResultContext<? extends V> context) {
        final V value = context.getResultObject();
        final MetaObject mo = MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
        // TODO is that assignment always true?
        final K key = (K) mo.getValue(mapKey);
        mappedResults.put(key, value);
    }

    public Map<K, V> getMappedResults() {
        return mappedResults;
    }
}
