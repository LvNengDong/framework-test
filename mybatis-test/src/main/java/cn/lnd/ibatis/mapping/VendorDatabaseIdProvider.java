package cn.lnd.ibatis.mapping;

import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * @Author lnd
 * @Description  实现 DatabaseIdProvider 接口，供应商数据库标识提供器实现类。
 * @Date 2024/9/19 17:35
 */
public class VendorDatabaseIdProvider implements DatabaseIdProvider {

    private static final Log log = LogFactory.getLog(VendorDatabaseIdProvider.class);

    /**
     *  Properties 对象
     */
    private Properties properties;

    @Override
    public String getDatabaseId(DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource cannot be null");
        }
        try {
            // 获得数据库标识
            return getDatabaseName(dataSource);
        } catch (Exception e) {
            log.error("Could not get a databaseId from dataSource", e);
        }
        return null;
    }

    @Override
    public void setProperties(Properties p) {
        this.properties = p;
    }

    private String getDatabaseName(DataSource dataSource) throws SQLException {
        // <1> 获得数据库产品名
        String productName = getDatabaseProductName(dataSource);
        if (this.properties != null) {
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                if (productName.contains((String) property.getKey())) {
                    // 如果产品名包含 KEY ，则返回对应的  VALUE
                    return (String) property.getValue();
                }
            }
            // no match, return null
            return null;
        }
        // <3> 不存在 properties ，则直接返回 productName
        return productName;
    }

    private String getDatabaseProductName(DataSource dataSource) throws SQLException {
        Connection con = null;
        try {
            // 获得数据库连接
            con = dataSource.getConnection();
            // 获得数据库产品名
            DatabaseMetaData metaData = con.getMetaData();
            return metaData.getDatabaseProductName();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    // ignored
                }
            }
        }
    }

}
