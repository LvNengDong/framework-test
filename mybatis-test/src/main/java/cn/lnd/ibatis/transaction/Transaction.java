package cn.lnd.ibatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description
 * 连接相关
 *      #getConnection() 方法，获得连接。
 *      #close() 方法，关闭连接。
 * 事务相关
 *      #commit() 方法，事务提交。
 *      #rollback() 方法，事务回滚。
 *      #getTimeout() 方法，事务超时时间。实际上，目前这个方法都是空实现。
 * @Date 2024/9/17 23:16
 */
public interface Transaction {

    /**
     * 获得连接
     *
     * Retrieve inner database connection
     *
     * @return DataBase connection
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * 事务提交
     *
     * Commit inner database connection.
     *
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * 事务回滚
     *
     * Rollback inner database connection.
     *
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * 关闭连接
     *
     * Close inner database connection.
     *
     * @throws SQLException
     */
    void close() throws SQLException;

    /**
     * 获得事务超时时间
     *
     * Get transaction timeout if set
     *
     * @throws SQLException
     */
    Integer getTimeout() throws SQLException;

}

/*
    MyBatis 对数据库中的事务进行了抽象，其自身提供了相应的事务接口和简单实现。

    在很多场景中，MyBatis 会与 Spring 框架集成，并由 Spring 框架管理事务。

    org.mybatis.spring.transaction.SpringManagedTransaction ，实现 Transaction 接口，基于 Spring 管理的事务实现类。
    实际真正在使用的，本文暂时不分享，感兴趣的胖友可以自己先愁一愁 SpringManagedTransaction 。

    org.mybatis.spring.transaction.SpringManagedTransactionFactory ，实现 TransactionFactory 接口，SpringManagedTransaction 工厂实现类。
    实际真正在使用的，本文暂时不分享，感兴趣的胖友可以自己先愁一愁 SpringManagedTransactionFactory 。
*/