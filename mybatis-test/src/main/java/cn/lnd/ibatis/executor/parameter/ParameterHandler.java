package cn.lnd.ibatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 14:47
 */
public interface ParameterHandler {

    Object getParameterObject();

    void setParameters(PreparedStatement ps)
            throws SQLException;

}
