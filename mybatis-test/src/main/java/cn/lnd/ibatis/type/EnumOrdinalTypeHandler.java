package cn.lnd.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description
 *      java.lang.Enum 和 int 的互相转换
 * @Date 2024/9/19 16:23
 */
public class EnumOrdinalTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    /* 枚举类 */
    private Class<E> type;
    /**
     *  {@link #type} 下所有的枚举
     *  @see Class#getEnumConstants()
     */
    private final E[] enums;

    public EnumOrdinalTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
        this.enums = type.getEnumConstants();
        if (this.enums == null) {
            throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        // 将 Enum 转换成 int 类型
        ps.setInt(i, parameter.ordinal());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 获得 int 的值
        int i = rs.getInt(columnName);
        // 将 int 转换成 Enum 类型
        if (i == 0 && rs.wasNull()) {
            return null;
        } else {
            try {
                return enums[i];
            } catch (Exception ex) {
                throw new IllegalArgumentException("Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
            }
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 获得 int 的值
        int i = rs.getInt(columnIndex);
        // 将 int 转换成 Enum 类型
        if (i == 0 && rs.wasNull()) {
            return null;
        } else {
            try {
                return enums[i];
            } catch (Exception ex) {
                throw new IllegalArgumentException("Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
            }
        }
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 获得 int 的值
        int i = cs.getInt(columnIndex);
        // 将 int 转换成 Enum 类型
        if (i == 0 && cs.wasNull()) {
            return null;
        } else {
            try {
                return enums[i];
            } catch (Exception ex) {
                throw new IllegalArgumentException("Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
            }
        }
    }

}
