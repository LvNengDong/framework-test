package cn.lnd.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description 继承 BaseTypeHandler 抽象类，Enum 类型的 TypeHandler 实现类
 *      java.lang.Enum 和 java.util.String 的互相转换。
 *      因为数据库不存在枚举类型，所以讲枚举类型持久化到数据库有两种方式，Enum.name <=> String 和 Enum.ordinal <=> int 。我们目前看到的 EnumTypeHandler 是前者，下面我们将看到的 EnumOrdinalTypeHandler 是后者。
 * @Date 2024/9/19 16:23
 */
public class EnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    /* 枚举类 */
    private Class<E> type;

    public EnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        // 将 Enum 转换成 String 类型
        if (jdbcType == null) {
            ps.setString(i, parameter.name());
        } else {
            ps.setObject(i, parameter.name(), jdbcType.TYPE_CODE); // see r3589
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 获得 String 的值
        String s = rs.getString(columnName);
        // 将 String 转换成 Enum 类型
        return s == null ? null : Enum.valueOf(type, s);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 获得 String 的值
        String s = rs.getString(columnIndex);
        // 将 String 转换成 Enum 类型
        return s == null ? null : Enum.valueOf(type, s);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 获得 String 的值
        String s = cs.getString(columnIndex);
        // 将 String 转换成 Enum 类型
        return s == null ? null : Enum.valueOf(type, s);
    }
}
