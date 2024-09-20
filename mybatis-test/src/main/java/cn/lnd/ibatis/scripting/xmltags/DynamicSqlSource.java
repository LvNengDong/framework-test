package cn.lnd.ibatis.scripting.xmltags;

import cn.lnd.ibatis.builder.SqlSourceBuilder;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.session.Configuration;

import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 20:59
 */
public class DynamicSqlSource implements SqlSource {

    private Configuration configuration;
    private cn.lnd.ibatis.scripting.xmltags.SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        cn.lnd.ibatis.scripting.xmltags.DynamicContext context = new DynamicContext(configuration, parameterObject);
        rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }

}
