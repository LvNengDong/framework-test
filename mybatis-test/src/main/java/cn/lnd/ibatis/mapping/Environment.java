package cn.lnd.ibatis.mapping;

import cn.lnd.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 00:12
 */
public class Environment {
    /* 环境编号 */
    private final String id;
    /* TransactionFactory 对象 */
    private final TransactionFactory transactionFactory;
    /* DataSource 对象 */
    private final DataSource dataSource;

    public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        if (transactionFactory == null) {
            throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
        }
        this.id = id;
        if (dataSource == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        this.transactionFactory = transactionFactory;
        this.dataSource = dataSource;
    }

    public static class Builder {
        /* 环境编号 */
        private String id;
        /* TransactionFactory 对象 */
        private TransactionFactory transactionFactory;
        /* 环境编号 */
        private DataSource dataSource;

        public Builder(String id) {
            this.id = id;
        }

        public Environment.Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }

        public Environment.Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public String id() {
            return this.id;
        }

        public Environment build() {
            return new Environment(this.id, this.transactionFactory, this.dataSource);
        }

    }

    public String getId() {
        return this.id;
    }

    public TransactionFactory getTransactionFactory() {
        return this.transactionFactory;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

}
