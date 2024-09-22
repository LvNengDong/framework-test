package cn.lnd.ibatis.session;

import cn.lnd.ibatis.session.Configuration;
import cn.lnd.ibatis.session.ExecutorType;
import cn.lnd.ibatis.session.SqlSession;

import java.sql.Connection;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/18 23:32
 */
public interface SqlSessionFactory {
    SqlSession openSession();

    SqlSession openSession(boolean autoCommit);
    SqlSession openSession(Connection connection);
    SqlSession openSession(TransactionIsolationLevel level);

    SqlSession openSession(ExecutorType execType);
    SqlSession openSession(ExecutorType execType, boolean autoCommit);
    SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);
    SqlSession openSession(ExecutorType execType, Connection connection);

    Configuration getConfiguration();
}
