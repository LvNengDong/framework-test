package cn.lnd.ibatis.scripting.xmltags;

import cn.lnd.ibatis.builder.xml.XMLMapperEntityResolver;
import cn.lnd.ibatis.executor.parameter.ParameterHandler;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.mapping.MappedStatement;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.parsing.PropertyParser;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.parsing.XPathParser;
import cn.lnd.ibatis.scripting.LanguageDriver;
import cn.lnd.ibatis.scripting.defaults.DefaultParameterHandler;
import cn.lnd.ibatis.scripting.defaults.RawSqlSource;
import cn.lnd.ibatis.session.Configuration;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:04
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        // issue #3
        if (script.startsWith("<script>")) {
            XPathParser parser = new XPathParser(script, false, configuration.getVariables(), new XMLMapperEntityResolver());
            return createSqlSource(configuration, parser.evalNode("/script"), parameterType);
        } else {
            // issue #127
            script = PropertyParser.parse(script, configuration.getVariables());
            TextSqlNode textSqlNode = new TextSqlNode(script);
            if (textSqlNode.isDynamic()) {
                return new DynamicSqlSource(configuration, textSqlNode);
            } else {
                return new RawSqlSource(configuration, script, parameterType);
            }
        }
    }

}
