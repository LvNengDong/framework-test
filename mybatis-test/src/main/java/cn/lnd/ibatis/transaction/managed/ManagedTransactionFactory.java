package cn.lnd.ibatis.transaction.managed;

import cn.lnd.ibatis.transaction.Transaction;
import cn.lnd.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/17 23:28
 */
public class ManagedTransactionFactory implements TransactionFactory {

    /**
     * 是否关闭连接
     * */
    private boolean closeConnection = true;

    @Override
    public void setProperties(Properties props) {
        // 获得是否关闭连接属性
        if (props != null) {
            String closeConnectionProperty = props.getProperty("closeConnection");
            if (closeConnectionProperty != null) {
                closeConnection = Boolean.valueOf(closeConnectionProperty);
            }
        }
    }

    @Override
    public Transaction newTransaction(Connection conn) {
        // 创建 ManagedTransaction 对象
        return new ManagedTransaction(conn, closeConnection);
    }

    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
        // Silently ignores autocommit and isolation level, as managed transactions are entirely
        // controlled by an external manager.  It's silently ignored so that
        // code remains portable between managed and unmanaged configurations.
        // 创建 ManagedTransaction 对象
        return new ManagedTransaction(ds, level, closeConnection);
    }
}
