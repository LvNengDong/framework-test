package cn.lnd.ibatis.mapping;

import java.sql.ResultSet;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 15:10
 */
public enum ResultSetType {
    FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
    SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
    SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);

    private int value;

    ResultSetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
