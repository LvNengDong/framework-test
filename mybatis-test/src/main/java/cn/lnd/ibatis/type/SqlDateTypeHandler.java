package cn.lnd.ibatis.type;


import java.sql.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 16:25
 */
public class SqlDateTypeHandler extends BaseTypeHandler<Date> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setDate(i, parameter);
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return rs.getDate(columnName);
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return rs.getDate(columnIndex);
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return cs.getDate(columnIndex);
    }
}
