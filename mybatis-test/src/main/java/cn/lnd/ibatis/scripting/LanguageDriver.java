package cn.lnd.ibatis.scripting;

import cn.lnd.ibatis.executor.parameter.ParameterHandler;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.mapping.MappedStatement;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.scripting.defaults.DefaultParameterHandler;
import cn.lnd.ibatis.session.Configuration;

/**
 * @Author lnd
 * @Description 语言驱动接口
 * @Date 2024/9/19 14:46
 */
public interface LanguageDriver {

    /**
     * 创建 ParameterHandler 对象。
     *
     * @see DefaultParameterHandler
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

    /**
     * 根据 Mapper XML 配置的 Statement 标签中，如 <select /> 等，创建 SqlSource 对象，
     *
     * @return
     */
    SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);

    /**
     * 根据方法注解配置，如 @Select 等，创建 SqlSource 对象，
     *
     * @return
     */
    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

}
