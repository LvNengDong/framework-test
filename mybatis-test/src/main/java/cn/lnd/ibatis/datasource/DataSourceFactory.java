package cn.lnd.ibatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 22:09
 */
public interface DataSourceFactory {
    /**
     * 设置 DataSource 对象的属性
     *
     * @param props 属性
     */
    void setProperties(Properties props);

    /**
     * 获得 DataSource 对象
     *
     * @return DataSource 对象
     */
    DataSource getDataSource();
}
