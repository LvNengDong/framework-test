package cn.lnd.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description 类型转换处理器
 *      一共有两类方法，分别是：
 *          #setParameter(...) 方法，是 Java Type => JDBC Type 的过程。
 *          #getResult(...) 方法，是 JDBC Type => Java Type 的过程。
 * @Date 2024/9/14 22:33
 */
public interface TypeHandler<T> {
    /**
     *  在通过PreparedStatement为SQL语句绑定参数时，会将传入的实参数据由Java类型转换成JdbcType类型
     *
     *  Java Type => JDBC Type
     *
     * @param ps PreparedStatement 对象
     * @param i 参数占位符的位置
     * @param parameter 参数
     * @param jdbcType JDBC 类型
     * @throws SQLException 当发生 SQL 异常时
     */
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     *  从ResultSet中获取数据时会使用getResult()方法，其中会将读取到的数据由JdbcType类型转换成Java类型
     *
     *  JDBC Type => Java Type
     *
     * @param rs ResultSet 对象
     * @param columnName 字段名
     * @return
     * @throws SQLException
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 功能同上
     *
     * JDBC Type => Java Type
     *
     * @param rs ResultSet 对象
     * @param columnIndex 字段位置
     * @return 值
     * @throws SQLException
     */
    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * 获得 CallableStatement 的指定字段的值
     *
     * JDBC Type => Java Type
     *
     * @param cs CallableStatement 对象，支持调用存储过程
     * @param columnIndex 字段位置
     * @return 值
     * @throws SQLException
     */
    T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
