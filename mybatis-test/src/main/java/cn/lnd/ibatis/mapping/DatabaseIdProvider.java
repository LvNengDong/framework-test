package cn.lnd.ibatis.mapping;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 00:12
 */
public interface DatabaseIdProvider {

    void setProperties(Properties p);

    String getDatabaseId(DataSource dataSource) throws SQLException;
}
