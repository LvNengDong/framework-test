package cn.lnd.tmp;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * @Author lnd
 * @Description 解析全局配置文件，并保存到一个内存对象中
 * @Date 2023/2/12 23:15
 */
@Data
public class XMLConfigurerBuilder {
    private Configuration configuration;

    public XMLConfigurerBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 使用dom4j解析配置文件    1、全局配置文件    2、Mapper.xml配置文件
     */
    public Configuration parseConfiguration(InputStream inputStream) throws DocumentException, PropertyVetoException {
        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();// <configuration>

        // 解析<property>标签
        List<Element> propertyElements = rootElement.selectNodes("//property");
        Properties properties = new Properties();
        for (Element propertyElement : propertyElements) {
            String name = propertyElement.attributeValue("name");
            String value = propertyElement.attributeValue("value");
            properties.setProperty(name, value);
        }
        // 根据解析得到的配置项，创建【数据库连接池】
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(properties.getProperty("driverClass"));
        dataSource.setJdbcUrl(properties.getProperty("jdbcUrl"));
        dataSource.setUser(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));

        // 将上下文配置信息填充到 configuration 对象中
        configuration.setDataSource(dataSource);

        return null;

    }
}
