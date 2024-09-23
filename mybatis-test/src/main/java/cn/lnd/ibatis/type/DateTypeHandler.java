package cn.lnd.ibatis.type;


import java.sql.*;
import java.util.Date;

/**
 * @Author lnd
 * @Description java.util.Date 和 java.sql.Timestamp 的互相转换
 * @Date 2024/9/19 16:22
 */
public class DateTypeHandler extends BaseTypeHandler<Date> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
            throws SQLException {
        // 将 Date 转换成 Timestamp 类型
        // 然后设置到 ps 中
        ps.setTimestamp(i, new Timestamp((parameter).getTime()));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        // 获得 Timestamp 的值
        Timestamp sqlTimestamp = rs.getTimestamp(columnName);
        // 将 Timestamp 转换成 Date 类型
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        // 获得 Timestamp 的值
        Timestamp sqlTimestamp = rs.getTimestamp(columnIndex);
        // 将 Timestamp 转换成 Date 类型
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        // 获得 Timestamp 的值
        Timestamp sqlTimestamp = cs.getTimestamp(columnIndex);
        // 将 Timestamp 转换成 Date 类型
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }
}
