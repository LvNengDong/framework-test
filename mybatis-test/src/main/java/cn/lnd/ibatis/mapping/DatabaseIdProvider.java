package cn.lnd.ibatis.mapping;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @Author lnd
 * @Description 数据库厂商标识提供器接口
 * @Date 2024/9/19 00:12
 */
public interface DatabaseIdProvider {

    /**
     * 设置属性
     *
     * @param p Properties 对象
     */
    void setProperties(Properties p);

    /**
     * 获得数据库标识
     *
     * @param dataSource 数据源
     * @return 数据库标识
     * @throws SQLException 当 DB 发生异常时
     */
    String getDatabaseId(DataSource dataSource) throws SQLException;
}
