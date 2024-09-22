package cn.lnd.ibatis.transaction.jdbc;

import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;
import cn.lnd.ibatis.transaction.Transaction;
import cn.lnd.ibatis.transaction.TransactionException;
import cn.lnd.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/17 23:18
 */
public class JdbcTransaction implements Transaction {

    private static final Log log = LogFactory.getLog(JdbcTransaction.class);

    /**
     * Connection 对象
     */
    protected Connection connection;
    /**
     * DataSource 对象
     */
    protected DataSource dataSource;
    /**
     * 事务隔离级别
     */
    protected TransactionIsolationLevel level;
    /**
     * 是否自动提交
     */
    protected boolean autoCommit;

    public JdbcTransaction(DataSource ds, TransactionIsolationLevel desiredLevel, boolean desiredAutoCommit) {
        dataSource = ds;
        level = desiredLevel;
        autoCommit = desiredAutoCommit;
    }

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // 连接为空，进行创建
        if (connection == null) {
            openConnection();
        }
        return connection;
    }

    @Override
    public void commit() throws SQLException {
        // 非自动提交，则执行提交事务
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Committing JDBC Connection [" + connection + "]");
            }
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        // 非自动提交。则回滚事务
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling back JDBC Connection [" + connection + "]");
            }
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            // 重置连接为自动提交
            resetAutoCommit();
            if (log.isDebugEnabled()) {
                log.debug("Closing JDBC Connection [" + connection + "]");
            }
            // 关闭连接
            connection.close();
        }
    }

    /**
     * 设置指定的 autoCommit 属性
     *
     * @param desiredAutoCommit 指定的 autoCommit 属性
     */
    protected void setDesiredAutoCommit(boolean desiredAutoCommit) {
        try {
            if (connection.getAutoCommit() != desiredAutoCommit) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting autocommit to " + desiredAutoCommit + " on JDBC Connection [" + connection + "]");
                }
                connection.setAutoCommit(desiredAutoCommit);
            }
        } catch (SQLException e) {
            // Only a very poorly implemented driver would fail here,
            // and there's not much we can do about that.
            throw new TransactionException("Error configuring AutoCommit.  "
                    + "Your driver may not support getAutoCommit() or setAutoCommit(). "
                    + "Requested setting: " + desiredAutoCommit + ".  Cause: " + e, e);
        }
    }

    /**
     * 重置 autoCommit 属性
     */
    protected void resetAutoCommit() {
        try {
            if (!connection.getAutoCommit()) {
                // MyBatis does not call commit/rollback on a connection if just selects were performed.
                // Some databases start transactions with select statements
                // and they mandate a commit/rollback before closing the connection.
                // A workaround is setting the autocommit to true before closing the connection.
                // Sybase throws an exception here.
                if (log.isDebugEnabled()) {
                    log.debug("Resetting autocommit to true on JDBC Connection [" + connection + "]");
                }
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error resetting autocommit to true "
                        + "before closing the connection.  Cause: " + e);
            }
        }
    }

    /**
     * 获得 Connection 对象
     *
     * @throws SQLException 获得失败
     */
    protected void openConnection() throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Opening JDBC Connection");
        }
        // 获得连接
        connection = dataSource.getConnection();
        // 设置隔离级别
        if (level != null) {
            connection.setTransactionIsolation(level.getLevel());
        }
        // 设置 autoCommit 属性
        setDesiredAutoCommit(autoCommit);
    }

    @Override
    public Integer getTimeout() throws SQLException {
        return null;
    }

}
