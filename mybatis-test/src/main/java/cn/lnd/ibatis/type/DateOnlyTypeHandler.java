package cn.lnd.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @Author lnd
 * @Description
 *      继承 BaseTypeHandler 抽象类，Date 类型的 TypeHandler 实现类
 *      java.util.Date 和 java.sql.Date 的互相转换
 *      数据库里的时间有多种类型，以 MySQL 举例子，有 date、timestamp、datetime 三种类型
 * @Date 2024/9/19 16:22
 */
public class DateOnlyTypeHandler extends BaseTypeHandler<Date> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setDate(i, new java.sql.Date((parameter.getTime())));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        // 获得 sql Date 的值
        java.sql.Date sqlDate = rs.getDate(columnName);
        // 将 sql Date 转换成 java Date 类型
        if (sqlDate != null) {
            return new java.util.Date(sqlDate.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        // 获得 sql Date 的值
        java.sql.Date sqlDate = rs.getDate(columnIndex);
        // 将 sql Date 转换成 java Date 类型
        if (sqlDate != null) {
            return new java.util.Date(sqlDate.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        // 获得 sql Date 的值
        java.sql.Date sqlDate = cs.getDate(columnIndex);
        // 将 sql Date 转换成 java Date 类型
        if (sqlDate != null) {
            return new java.util.Date(sqlDate.getTime());
        }
        return null;
    }

}
