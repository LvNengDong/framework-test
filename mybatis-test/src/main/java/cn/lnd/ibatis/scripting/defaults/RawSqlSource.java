package cn.lnd.ibatis.scripting.defaults;

import cn.lnd.ibatis.builder.SqlSourceBuilder;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.scripting.xmltags.DynamicContext;
import cn.lnd.ibatis.scripting.xmltags.SqlNode;
import cn.lnd.ibatis.session.Configuration;

import java.util.HashMap;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:06
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<String, Object>());
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

}
