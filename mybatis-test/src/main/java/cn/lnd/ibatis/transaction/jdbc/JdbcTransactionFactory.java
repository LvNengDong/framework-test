package cn.lnd.ibatis.transaction.jdbc;

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
public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public void setProperties(Properties props) {
    }

    @Override
    public Transaction newTransaction(Connection conn) {
        // 创建 JdbcTransaction 对象
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
        // 创建 JdbcTransaction 对象
        return new JdbcTransaction(ds, level, autoCommit);
    }

}
