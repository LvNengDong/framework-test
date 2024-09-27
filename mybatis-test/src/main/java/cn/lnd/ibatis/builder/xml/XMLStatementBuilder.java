package cn.lnd.ibatis.builder.xml;

import cn.lnd.ibatis.builder.BaseBuilder;
import cn.lnd.ibatis.builder.MapperBuilderAssistant;
import cn.lnd.ibatis.executor.keygen.Jdbc3KeyGenerator;
import cn.lnd.ibatis.executor.keygen.KeyGenerator;
import cn.lnd.ibatis.executor.keygen.NoKeyGenerator;
import cn.lnd.ibatis.executor.keygen.SelectKeyGenerator;
import cn.lnd.ibatis.mapping.*;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.scripting.LanguageDriver;
import cn.lnd.ibatis.session.Configuration;

import java.util.List;
import java.util.Locale;

/**
 * @Author lnd
 * @Description
 *
 *      主要负责解析 Mapper.xml 文件中的 <select/>、<insert/>、<update/>、<delete/> 标签。
 *
 * @Date 2024/9/19 16:55
 */
public class XMLStatementBuilder extends BaseBuilder {

    private MapperBuilderAssistant builderAssistant;
    private XNode context;
    private String requiredDatabaseId;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context) {
        this(configuration, builderAssistant, context, null);
    }

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context, String databaseId) {
        super(configuration);
        this.builderAssistant = builderAssistant;
        this.context = context;
        this.requiredDatabaseId = databaseId;
    }

    /**
     * 解析 SQL 标签的入口方法
     */
    public void parseStatementNode() {
        // 获取SQL标签的id以及databaseId属性
        String id = context.getStringAttribute("id");
        String databaseId = context.getStringAttribute("databaseId");

        // 若databaseId属性值与当前使用的数据库不匹配，则不加载该SQL标签；若存在相同id且databaseId不为空的SQL标签，则不再加载该SQL标签（避免重复加载）
        if (!databaseIdMatchesCurrent(id, databaseId, this.requiredDatabaseId)) {
            return;
        }

        // 获取SQL标签的属性值，例如，fetchSize、timeout、parameterType、parameterMap、resultMap、resultType、lang、resultSetType、flushCache、useCache等。这些属性的具体含义在MyBatis官方文档中已经有比较详细的介绍了，这里不再赘述
        Integer fetchSize = context.getIntAttribute("fetchSize");
        Integer timeout = context.getIntAttribute("timeout");
        String parameterMap = context.getStringAttribute("parameterMap");
        String parameterType = context.getStringAttribute("parameterType");
        Class<?> parameterTypeClass = resolveClass(parameterType);
        String resultMap = context.getStringAttribute("resultMap");
        String resultType = context.getStringAttribute("resultType");
        String lang = context.getStringAttribute("lang");
        LanguageDriver langDriver = getLanguageDriver(lang);

        Class<?> resultTypeClass = resolveClass(resultType);
        String resultSetType = context.getStringAttribute("resultSetType");
        StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
        ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);

        // 根据SQL标签的名称决定其 SqlCommandType
        String nodeName = context.getNode().getNodeName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
        boolean useCache = context.getBooleanAttribute("useCache", isSelect);
        boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);

        // 在解析SQL语句之前，先处理其中的<include>标签（在实际应用中，我们会在 <sql> 标签中定义一些能够被重用的SQL片段，在解析SQL标签之前，MyBatis 会先将 <include> 标签转换成对应的 SQL 片段）
        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
        includeParser.applyIncludes(context.getNode());

        // 解析<selectKey>标签
        // （在有的数据库表设计场景中，我们会添加一个自增 ID 字段作为主键，在某些业务逻辑里面，我们希望在执行 insert 语句的时候返回这个自增 ID 值，<selectKey> 标签就可以实现自增 ID 的获取。<selectKey> 标签不仅可以获取自增 ID，还可以指定其他 SQL 语句，从其他表或执行数据库的函数获取字段值。）
        // Parse selectKey after includes and remove them.
        processSelectKeyNodes(id, parameterTypeClass, langDriver);

        // 下面是解析SQL语句的逻辑，也是parseStatementNode()方法的核心
        // 通过LanguageDriver.createSqlSource()方法创建SqlSource对象
        // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
        // 获取SQL标签中配置的resultSets、keyProperty、keyColumn等属性，以及前面解析<selectKey>标签得到的KeyGenerator对象等，
        // 这些信息将会填充到MappedStatement对象中
        // 根据上述属性信息创建MappedStatement对象，并添加到Configuration.mappedStatements集合中保存
        String resultSets = context.getStringAttribute("resultSets");
        String keyProperty = context.getStringAttribute("keyProperty");
        String keyColumn = context.getStringAttribute("keyColumn");
        KeyGenerator keyGenerator;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
                    configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
                    ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
        }

        builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
                fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
                resultSetTypeEnum, flushCache, useCache, resultOrdered,
                keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets);
    }

    private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        List<XNode> selectKeyNodes = context.evalNodes("selectKey");
        if (configuration.getDatabaseId() != null) {
            parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver, configuration.getDatabaseId());
        }
        parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver, null);
        removeSelectKeyNodes(selectKeyNodes);
    }

    private void parseSelectKeyNodes(String parentId, List<XNode> list, Class<?> parameterTypeClass, LanguageDriver langDriver, String skRequiredDatabaseId) {
        for (XNode nodeToHandle : list) {
            String id = parentId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            String databaseId = nodeToHandle.getStringAttribute("databaseId");
            if (databaseIdMatchesCurrent(id, databaseId, skRequiredDatabaseId)) {
                parseSelectKeyNode(id, nodeToHandle, parameterTypeClass, langDriver, databaseId);
            }
        }
    }

    private void parseSelectKeyNode(String id, XNode nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver, String databaseId) {
        // 解析<selectKey>标签的resultType、statementType、keyProperty等属性
        // 通过LanguageDriver解析<selectKey>标签中的SQL语句，得到对应的SqlSource对象
        String resultType = nodeToHandle.getStringAttribute("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        StatementType statementType = StatementType.valueOf(nodeToHandle.getStringAttribute("statementType", StatementType.PREPARED.toString()));
        String keyProperty = nodeToHandle.getStringAttribute("keyProperty");
        String keyColumn = nodeToHandle.getStringAttribute("keyColumn");
        boolean executeBefore = "BEFORE".equals(nodeToHandle.getStringAttribute("order", "AFTER"));

        //defaults
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        Integer fetchSize = null;
        Integer timeout = null;
        boolean flushCache = false;
        String parameterMap = null;
        String resultMap = null;
        ResultSetType resultSetTypeEnum = null;


        SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        // 创建MappedStatement对象
        builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
                fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
                resultSetTypeEnum, flushCache, useCache, resultOrdered,
                keyGenerator, keyProperty, keyColumn, databaseId, langDriver, null);

        id = builderAssistant.applyCurrentNamespace(id, false);

        // 创建<selectKey>标签对应的KeyGenerator对象，这个KeyGenerator对象会添加到Configuration.keyGenerators集合中
        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
        configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
    }

    private void removeSelectKeyNodes(List<XNode> selectKeyNodes) {
        for (XNode nodeToHandle : selectKeyNodes) {
            nodeToHandle.getParent().getNode().removeChild(nodeToHandle.getNode());
        }
    }

    private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
        if (requiredDatabaseId != null) {
            if (!requiredDatabaseId.equals(databaseId)) {
                return false;
            }
        } else {
            if (databaseId != null) {
                return false;
            }
            // skip this statement if there is a previous one with a not null databaseId
            id = builderAssistant.applyCurrentNamespace(id, false);
            if (this.configuration.hasStatement(id, false)) {
                MappedStatement previous = this.configuration.getMappedStatement(id, false); // issue #2
                if (previous.getDatabaseId() != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private LanguageDriver getLanguageDriver(String lang) {
        Class<? extends LanguageDriver> langClass = null;
        if (lang != null) {
            langClass = resolveClass(lang);
        }
        return builderAssistant.getLanguageDriver(langClass);
    }

}
