package cn.lnd.ibatis.type;



import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 16:25
 */
public class ShortTypeHandler extends BaseTypeHandler<Short> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Short parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setShort(i, parameter);
    }

    @Override
    public Short getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return rs.getShort(columnName);
    }

    @Override
    public Short getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return rs.getShort(columnIndex);
    }

    @Override
    public Short getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return cs.getShort(columnIndex);
    }
}
