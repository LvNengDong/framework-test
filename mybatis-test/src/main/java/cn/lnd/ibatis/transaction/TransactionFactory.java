package cn.lnd.ibatis.transaction;

import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/17 23:27
 */
public interface TransactionFactory {

    /**
     * Sets transaction factory custom properties.
     *
     * 设置工厂的属性
     *
     * @param props 属性
     */
    void setProperties(Properties props);

    /**
     * Creates a {@link Transaction} out of an existing connection.
     *
     * 创建 Transaction 事务
     *
     * @param conn Existing database connection
     * @return Transaction
     * @since 3.1.0
     */
    Transaction newTransaction(Connection conn);

    /**
     * Creates a {@link Transaction} out of a datasource.
     *
     * 创建 Transaction 事务
     *
     * @param dataSource DataSource to take the connection from
     * @param level      Desired isolation level
     * @param autoCommit Desired autocommit
     * @return Transaction
     * @since 3.1.0
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}

