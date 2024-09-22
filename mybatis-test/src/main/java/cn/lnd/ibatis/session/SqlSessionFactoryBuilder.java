package cn.lnd.ibatis.session;

import cn.lnd.ibatis.builder.xml.XMLConfigBuilder;
import cn.lnd.ibatis.exceptions.ExceptionFactory;
import cn.lnd.ibatis.executor.ErrorContext;
import cn.lnd.ibatis.session.defaults.DefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * @Author lnd
 * @Description 构造 SqlSessionFactory 对象
 * @Date 2024/9/18 23:31
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }

    /**
     * >>> MyBatis 初始化流程入口 <<<
     *
     * 构造 SqlSessionFactory 对象
     *
     * @param reader Reader 对象
     * @param environment environment 环境
     * @param properties properties Properties 变量
     * @return SqlSessionFactory 对象
     */
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            // <1> 创建 XMLConfigBuilder 对象 【加载配置文件】
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            // <2> 执行 XML 解析 [ 调用 XMLConfigBuilder#parse() 方法，执行 XML 解析，返回 Configuration 对象。] 【解析配置文件】
            // <3> 创建 DefaultSqlSessionFactory 对象
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    /**
     * 构造者模式：将复杂的参数包装成一个 Configuration 对象，在创建 DefaultSqlSessionFactory 只需要传入一个 Configuration 对象即可
     * @param config
     * @return
     */
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

}
