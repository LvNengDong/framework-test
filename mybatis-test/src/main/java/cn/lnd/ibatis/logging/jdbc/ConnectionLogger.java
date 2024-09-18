package cn.lnd.ibatis.logging.jdbc;


import cn.lnd.ibatis.logging.Log;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 21:44
 */
public final class ConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

    private Connection connection;

    private ConnectionLogger(Connection conn, Log statementLog, int queryStack) {
        super(statementLog, queryStack);
        this.connection = conn;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params)
            throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, params);
            }
            if ("prepareStatement".equals(method.getName())) {
                if (isDebugEnabled()) {
                    debug(" Preparing: " + removeBreakingWhitespace((String) params[0]), true);
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = PreparedStatementLogger.newInstance(stmt, statementLog, queryStack);
                return stmt;
            } else if ("prepareCall".equals(method.getName())) {
                if (isDebugEnabled()) {
                    debug(" Preparing: " + removeBreakingWhitespace((String) params[0]), true);
                }
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = PreparedStatementLogger.newInstance(stmt, statementLog, queryStack);
                return stmt;
            } else if ("createStatement".equals(method.getName())) {
                Statement stmt = (Statement) method.invoke(connection, params);
                stmt = StatementLogger.newInstance(stmt, statementLog, queryStack);
                return stmt;
            } else {
                return method.invoke(connection, params);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
    }

    /*
     * Creates a logging version of a connection
     *
     * @param conn - the original connection
     * @return - the connection with logging
     */
    public static Connection newInstance(Connection conn, Log statementLog, int queryStack) {
        InvocationHandler handler = new ConnectionLogger(conn, statementLog, queryStack);
        ClassLoader cl = Connection.class.getClassLoader();
        return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
    }

    /*
     * return the wrapped connection
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

}
