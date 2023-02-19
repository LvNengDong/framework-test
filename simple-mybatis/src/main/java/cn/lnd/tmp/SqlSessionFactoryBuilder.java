package cn.lnd.tmp;

import lombok.Data;
import org.dom4j.DocumentException;

import java.io.InputStream;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/12 22:43
 */
@Data
public class SqlSessionFactoryBuilder {

    private Configuration configuration;

    /**
     * 1、解析配置文件，将解析出来的内容封装到 Configuration 和 MappedStatement 中
     * 2、创建 SqlSessionFactory 的实现类 DefaultSqlSession
     */
    public SqlSessionFactory build(InputStream inputStream) throws Exception {
        // 1、解析配置文件，并封装到 configuration 中
        XMLConfigurerBuilder xmlConfigurerBuilder = new XMLConfigurerBuilder(configuration);
        Configuration configuration = xmlConfigurerBuilder.parseConfiguration(inputStream);

        // 2、创建SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        return sqlSessionFactory;
    }
}
