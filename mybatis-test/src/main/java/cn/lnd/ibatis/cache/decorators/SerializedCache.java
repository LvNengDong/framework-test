package cn.lnd.ibatis.cache.decorators;

import cn.lnd.ibatis.cache.Cache;
import cn.lnd.ibatis.cache.CacheException;
import cn.lnd.ibatis.io.Resources;

import java.io.*;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:33
 */
public class SerializedCache implements Cache {

    private Cache delegate;

    public SerializedCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public void putObject(Object key, Object object) {
        if (object == null || object instanceof Serializable) {
            delegate.putObject(key, serialize((Serializable) object));
        } else {
            throw new CacheException("SharedCache failed to make a copy of a non-serializable object: " + object);
        }
    }

    @Override
    public Object getObject(Object key) {
        Object object = delegate.getObject(key);
        return object == null ? null : deserialize((byte[]) object);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    private byte[] serialize(Serializable value) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            oos.close();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new CacheException("Error serializing object.  Cause: " + e, e);
        }
    }

    private Serializable deserialize(byte[] value) {
        Serializable result;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(value);
            ObjectInputStream ois = new cn.lnd.ibatis.cache.decorators.SerializedCache.CustomObjectInputStream(bis);
            result = (Serializable) ois.readObject();
            ois.close();
        } catch (Exception e) {
            throw new CacheException("Error deserializing object.  Cause: " + e, e);
        }
        return result;
    }

    public static class CustomObjectInputStream extends ObjectInputStream {

        public CustomObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            return Resources.classForName(desc.getName());
        }

    }

}
