package cn.lnd.ibatis.executor.statement;

import cn.lnd.ibatis.cursor.Cursor;
import cn.lnd.ibatis.executor.Executor;
import cn.lnd.ibatis.executor.ExecutorException;
import cn.lnd.ibatis.executor.keygen.KeyGenerator;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.mapping.MappedStatement;
import cn.lnd.ibatis.mapping.ParameterMapping;
import cn.lnd.ibatis.mapping.ParameterMode;
import cn.lnd.ibatis.session.ResultHandler;
import cn.lnd.ibatis.session.RowBounds;
import cn.lnd.ibatis.type.JdbcType;

import java.sql.*;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 20:59
 */
public class CallableStatementHandler extends BaseStatementHandler {

    public CallableStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        CallableStatement cs = (CallableStatement) statement;
        cs.execute();
        int rows = cs.getUpdateCount();
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, cs, parameterObject);
        resultSetHandler.handleOutputParameters(cs);
        return rows;
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        CallableStatement cs = (CallableStatement) statement;
        cs.addBatch();
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        CallableStatement cs = (CallableStatement) statement;
        cs.execute();
        List<E> resultList = resultSetHandler.<E>handleResultSets(cs);
        resultSetHandler.handleOutputParameters(cs);
        return resultList;
    }

    @Override
    public <E> Cursor<E> queryCursor(Statement statement) throws SQLException {
        CallableStatement cs = (CallableStatement) statement;
        cs.execute();
        Cursor<E> resultList = resultSetHandler.<E>handleCursorResultSets(cs);
        resultSetHandler.handleOutputParameters(cs);
        return resultList;
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        if (mappedStatement.getResultSetType() != null) {
            return connection.prepareCall(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
        } else {
            return connection.prepareCall(sql);
        }
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        registerOutputParameters((CallableStatement) statement);
        parameterHandler.setParameters((CallableStatement) statement);
    }

    private void registerOutputParameters(CallableStatement cs) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        for (int i = 0, n = parameterMappings.size(); i < n; i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
                if (null == parameterMapping.getJdbcType()) {
                    throw new ExecutorException("The JDBC Type must be specified for output parameter.  Parameter: " + parameterMapping.getProperty());
                } else {
                    if (parameterMapping.getNumericScale() != null && (parameterMapping.getJdbcType() == JdbcType.NUMERIC || parameterMapping.getJdbcType() == JdbcType.DECIMAL)) {
                        cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getNumericScale());
                    } else {
                        if (parameterMapping.getJdbcTypeName() == null) {
                            cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE);
                        } else {
                            cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getJdbcTypeName());
                        }
                    }
                }
            }
        }
    }

}
