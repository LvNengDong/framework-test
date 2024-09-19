package cn.lnd.ibatis.executor.statement;

import cn.lnd.ibatis.cursor.Cursor;
import cn.lnd.ibatis.executor.parameter.ParameterHandler;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 21:01
 */
public interface StatementHandler {

    Statement prepare(Connection connection, Integer transactionTimeout)
            throws SQLException;

    void parameterize(Statement statement)
            throws SQLException;

    void batch(Statement statement)
            throws SQLException;

    int update(Statement statement)
            throws SQLException;

    <E> List<E> query(Statement statement, ResultHandler resultHandler)
            throws SQLException;

    <E> Cursor<E> queryCursor(Statement statement)
            throws SQLException;

    BoundSql getBoundSql();

    ParameterHandler getParameterHandler();

}
