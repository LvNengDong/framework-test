package cn.lnd.ibatis.transaction.managed;

import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;
import cn.lnd.ibatis.transaction.Transaction;
import cn.lnd.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description 和 JdbcTransaction 相比，少了 autoCommit 属性，空实现 #commit() 和 #rollback() 方法。因此，事务的管理，交给了容器。
 * @Date 2024/9/17 23:24
 */
public class ManagedTransaction implements Transaction {

    private static final Log log = LogFactory.getLog(ManagedTransaction.class);

    /**
     * Connection 对象
     */
    private Connection connection;
    /**
     * DataSource 对象
     */
    private DataSource dataSource;
    /**
     * 事务隔离级别
     */
    private TransactionIsolationLevel level;
    /**
     * 是否关闭连接
     *
     * 这个属性是和 {@link org.apache.ibatis.transaction.jdbc.JdbcTransaction} 不同的
     */
    private final boolean closeConnection;

    public ManagedTransaction(Connection connection, boolean closeConnection) {
        this.connection = connection;
        this.closeConnection = closeConnection;
    }

    public ManagedTransaction(DataSource ds, TransactionIsolationLevel level, boolean closeConnection) {
        this.dataSource = ds;
        this.level = level;
        this.closeConnection = closeConnection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        // 连接为空，进行创建
        if (this.connection == null) {
            openConnection();
        }
        return this.connection;
    }

    @Override
    public void commit() throws SQLException {
        // Does nothing
    }

    @Override
    public void rollback() throws SQLException {
        // Does nothing
    }

    @Override
    public void close() throws SQLException {
        // 如果开启关闭连接功能，则关闭连接
        if (this.closeConnection && this.connection != null) {
            if (log.isDebugEnabled()) {
                log.debug("Closing JDBC Connection [" + this.connection + "]");
            }
            this.connection.close();
        }
    }

    protected void openConnection() throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Opening JDBC Connection");
        }
        // 获得连接
        this.connection = this.dataSource.getConnection();
        // 设置隔离级别
        if (this.level != null) {
            this.connection.setTransactionIsolation(this.level.getLevel());
        }
    }

    @Override
    public Integer getTimeout() throws SQLException {
        return null;
    }

}
