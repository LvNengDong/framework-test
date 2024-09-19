package cn.lnd.ibatis.executor.resultset;

import cn.lnd.ibatis.cursor.Cursor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 20:50
 */
public interface ResultSetHandler {

    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

    void handleOutputParameters(CallableStatement cs) throws SQLException;

}
