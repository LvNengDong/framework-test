package cn.lnd.ibatis.cache;


/**
 * @Author lnd
 * @Description 继承 CacheKey 类，空缓存键
 * @Date 2024/9/18 21:53
 */
public final class NullCacheKey extends CacheKey {

    private static final long serialVersionUID = 3704229911977019465L;

    public NullCacheKey() {
        super();
    }

    @Override
    public void update(Object object) {
        throw new CacheException("Not allowed to update a NullCacheKey instance.");
    }

    @Override
    public void updateAll(Object[] objects) {
        throw new CacheException("Not allowed to update a NullCacheKey instance.");
    }
}
