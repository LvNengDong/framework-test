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
 *
 *      实现 LanguageDriver 接口，XML 语言驱动实现类
 *
 * @Date 2024/9/20 21:04
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        // 创建 DefaultParameterHandler 对象
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        // 创建 XMLScriptBuilder 对象，执行解析
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        // issue #3
        // <1> 如果是 <script> 开头，使用 XML 配置的方式，使用动态 SQL    【没使用过的胖友，可以看看 《spring boot(8)-mybatis三种动态sql》 。https://blog.csdn.net/wangb_java/article/details/73657958】
        if (script.startsWith("<script>")) {
            // <1.1> 创建 XPathParser 对象，解析出 <script /> 节点
            XPathParser parser = new XPathParser(script, false, configuration.getVariables(), new XMLMapperEntityResolver());
            // <1.2> 调用上面的 #createSqlSource(...) 方法，创建 SqlSource 对象
            return createSqlSource(configuration, parser.evalNode("/script"), parameterType);
            // <2>
        } else {
            // issue #127
            // <2.1> 变量替换
            script = PropertyParser.parse(script, configuration.getVariables());
            // <2.2> 创建 TextSqlNode 对象
            TextSqlNode textSqlNode = new TextSqlNode(script);
            // <2.3.1> 如果是动态 SQL ，则创建 DynamicSqlSource 对象
            if (textSqlNode.isDynamic()) {
                return new DynamicSqlSource(configuration, textSqlNode);
                // <2.3.2> 如果非动态 SQL ，则创建 RawSqlSource 对象
            } else {
                return new RawSqlSource(configuration, script, parameterType);
            }
        }
    }

}
