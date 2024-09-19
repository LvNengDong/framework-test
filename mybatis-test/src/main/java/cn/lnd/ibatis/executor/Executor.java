package cn.lnd.ibatis.executor;

import cn.lnd.ibatis.cache.CacheKey;
import cn.lnd.ibatis.cursor.Cursor;
import cn.lnd.ibatis.executor.BatchResult;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.mapping.MappedStatement;
import cn.lnd.ibatis.reflection.MetaObject;
import cn.lnd.ibatis.session.ResultHandler;
import cn.lnd.ibatis.session.RowBounds;
import cn.lnd.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 11:31
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    int update(MappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

    List<BatchResult> flushStatements() throws SQLException;

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    boolean isCached(MappedStatement ms, CacheKey key);

    void clearLocalCache();

    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    Transaction getTransaction();

    void close(boolean forceRollback);

    boolean isClosed();

    void setExecutorWrapper(cn.lnd.ibatis.executor.Executor executor);

}
