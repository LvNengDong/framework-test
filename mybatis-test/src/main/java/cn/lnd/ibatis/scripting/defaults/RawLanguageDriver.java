package cn.lnd.ibatis.scripting.defaults;

import cn.lnd.ibatis.builder.BuilderException;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.scripting.xmltags.XMLLanguageDriver;
import cn.lnd.ibatis.session.Configuration;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:05
 */
public class RawLanguageDriver extends XMLLanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        SqlSource source = super.createSqlSource(configuration, script, parameterType);
        checkIsNotDynamic(source);
        return source;
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        SqlSource source = super.createSqlSource(configuration, script, parameterType);
        checkIsNotDynamic(source);
        return source;
    }

    private void checkIsNotDynamic(SqlSource source) {
        if (!RawSqlSource.class.equals(source.getClass())) {
            throw new BuilderException("Dynamic content is not allowed when using RAW language");
        }
    }

}
