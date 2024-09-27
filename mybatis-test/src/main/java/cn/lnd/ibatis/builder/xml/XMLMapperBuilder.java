package cn.lnd.ibatis.builder.xml;

import cn.lnd.ibatis.builder.*;
import cn.lnd.ibatis.cache.Cache;
import cn.lnd.ibatis.executor.ErrorContext;
import cn.lnd.ibatis.io.Resources;
import cn.lnd.ibatis.mapping.*;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.parsing.XPathParser;
import cn.lnd.ibatis.session.Configuration;
import cn.lnd.ibatis.type.JdbcType;
import cn.lnd.ibatis.type.TypeHandler;

import java.io.InputStream;
import java.util.*;

/**
 * @Author lnd
 * @Description
 *      主要负责解析 Mapper 映射配置文件，形成 Builder 实例，为创建核心对象做准备
 * @Date 2024/9/18 23:54
 */
public class XMLMapperBuilder extends BaseBuilder {

    /* Java XPath 解析器 */
    private XPathParser parser;

    /* Mapper 构造器助手 */
    private MapperBuilderAssistant builderAssistant;

    /**
     * 可被其他语句引用的可重用语句块的集合
     *
     * 例如：<sql id="userColumns"> ${alias}.id,${alias}.username,${alias}.password </sql>
     */
    private Map<String, XNode> sqlFragments;

    /* 资源引用的地址 */
    private String resource;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
        this(inputStream, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        this(
                new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration,
                resource,
                sqlFragments
        );
    }

    private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.resource = resource;
    }

    /**
     * 解析 Mapper XML 配置文件
     */
    public void parse() {
        // <1> 判断当前 Mapper 是否已经加载过
        if (!configuration.isResourceLoaded(resource)) {
            // <2> 解析 `<mapper />` 节点【根节点】
            configurationElement(parser.evalNode("/mapper"));
            // <3> 标记该 Mapper 已经加载过
            configuration.addLoadedResource(resource);
            // <4> 绑定 Mapper
            bindMapperForNamespace();
        }

        // <5> 解析待定的 <resultMap /> 节点
        parsePendingResultMaps();
        // <6> 解析待定的 <cache-ref /> 节点
        parsePendingCacheRefs();
        // <7> 解析待定的 SQL 语句的节点
        parsePendingStatements();
    }

    public XNode getSqlFragment(String refid) {
        return sqlFragments.get(refid);
    }


    /**
     * 解析Mapper节点*/
    private void configurationElement(XNode context) {
        try {
            // <1> 获得 namespace 属性
            String namespace = context.getStringAttribute("namespace");
            if (namespace == null || namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            // <1> 设置 MapperBuilderAssistant#namespace 属性
            builderAssistant.setCurrentNamespace(namespace);
            // <2> 解析 <cache-ref /> 节点
            cacheRefElement(context.evalNode("cache-ref"));
            // <3> 解析 <cache /> 节点
            cacheElement(context.evalNode("cache"));
            // 已废弃！老式风格的参数映射。内联参数是首选,这个元素可能在将来被移除，这里不会记录。
            parameterMapElement(context.evalNodes("/mapper/parameterMap"));
            // <4> 解析 <resultMap /> 节点们
            resultMapElements(context.evalNodes("/mapper/resultMap"));
            // <5> 解析 <sql /> 节点们
            sqlElement(context.evalNodes("/mapper/sql"));
            // <6> 解析 <select /> <insert /> <update /> <delete /> 节点们
            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
        }
    }

    private void buildStatementFromContext(List<XNode> list) {
        if (configuration.getDatabaseId() != null) {
            buildStatementFromContext(list, configuration.getDatabaseId());
        }
        buildStatementFromContext(list, null);
    }

    private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
        for (XNode context : list) {
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
            try {
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteStatement(statementParser);
            }
        }
    }

    private void parsePendingResultMaps() {
        Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
        synchronized (incompleteResultMaps) {
            Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // ResultMap is still missing a resource...
                }
            }
        }
    }

    private void parsePendingChacheRefs() {
        Collection<CacheRefResolver> incompleteCacheRefs = configuration.getIncompleteCacheRefs();
        synchronized (incompleteCacheRefs) {
            Iterator<CacheRefResolver> iter = incompleteCacheRefs.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolveCacheRef();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Cache ref is still missing a resource...
                }
            }
        }
    }

    private void parsePendingStatements() {
        Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
        synchronized (incompleteStatements) {
            Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().parseStatementNode();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Statement is still missing a resource...
                }
            }
        }
    }

    /**
     * 解析 <cache /> 节点
     *      <cache-ref /> 标签用于引用其他命名空间中定义的缓存配置，以便在当前命名空间中共享缓存。
     *      通过 <cache-ref /> 标签，可以将当前命名空间中的查询结果缓存与其他命名空间中的缓存共享，避免重复缓存相同的数据，提高缓存的效率和一致性。
     * */
    private void cacheRefElement(XNode context) {
        if (context != null) {
            // <1> 获得 cache 指向的 namespace 名字，并添加到 configuration 的 cacheRefMap 中
            configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));
            // <2> 创建 CacheRefResolver 对象，并执行解析
            CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, context.getStringAttribute("namespace"));
            try {
                cacheRefResolver.resolveCacheRef();
            } catch (IncompleteElementException e) {
                // <3> 解析失败，添加到 configuration 的 incompleteCacheRefs 中
                configuration.addIncompleteCacheRef(cacheRefResolver);
            }
        }
    }


    /**
     * 解析 <cache /> 节点
     *      <cache /> 标签用于配置 MyBatis 的二级缓存，它可以提高查询性能，减少对数据库的访问次数。
     *      当使用 <cache /> 标签配置了二级缓存后，查询结果将会被缓存在内存中，下次相同的查询可以直接从缓存中获取结果，而不需要再次访问数据库。
     * */
    private void cacheElement(XNode context) throws Exception {
        if (context != null) {
            // <1> 获得负责存储的 Cache 实现类
            String type = context.getStringAttribute("type", "PERPETUAL");
            Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
            // <2> 获得负责过期的 Cache 实现类
            String eviction = context.getStringAttribute("eviction", "LRU");
            Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
            // <3> 获得 flushInterval、size、readWrite、blocking 属性
            Long flushInterval = context.getLongAttribute("flushInterval");
            Integer size = context.getIntAttribute("size");
            boolean readWrite = !context.getBooleanAttribute("readOnly", false);
            boolean blocking = context.getBooleanAttribute("blocking", false);
            // <4> 获得 Properties 属性
            Properties props = context.getChildrenAsProperties();
            // <5> 创建 Cache 对象
            builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
        }
    }

    private void parameterMapElement(List<XNode> list) throws Exception {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            String type = parameterMapNode.getStringAttribute("type");
            Class<?> parameterClass = resolveClass(type);
            List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
            List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
            for (XNode parameterNode : parameterNodes) {
                String property = parameterNode.getStringAttribute("property");
                String javaType = parameterNode.getStringAttribute("javaType");
                String jdbcType = parameterNode.getStringAttribute("jdbcType");
                String resultMap = parameterNode.getStringAttribute("resultMap");
                String mode = parameterNode.getStringAttribute("mode");
                String typeHandler = parameterNode.getStringAttribute("typeHandler");
                Integer numericScale = parameterNode.getIntAttribute("numericScale");
                ParameterMode modeEnum = resolveParameterMode(mode);
                Class<?> javaTypeClass = resolveClass(javaType);
                JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
                @SuppressWarnings("unchecked")
                Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
                ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);
                parameterMappings.add(parameterMapping);
            }
            builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
        }
    }

    /**
     * 解析 <resultMap /> 节点们
     * */
    private void resultMapElements(List<XNode> list) throws Exception {
        // 遍历 <resultMap /> 节点们
        for (XNode resultMapNode : list) {
            try {
                // 处理单个 <resultMap /> 节点
                resultMapElement(resultMapNode);
            } catch (IncompleteElementException e) {
                // ignore, it will be retried
            }
        }
    }

    private ResultMap resultMapElement(XNode resultMapNode) throws Exception {
        return resultMapElement(resultMapNode, Collections.emptyList());
    }

    /**
     * 解析 <resultMap /> 节点
     * */
    private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
        // <1> 获得 id 属性
        String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
        // <1> 获得 type 属性
        String type = resultMapNode.getStringAttribute("type",
                resultMapNode.getStringAttribute("ofType",
                        resultMapNode.getStringAttribute("resultType",
                                resultMapNode.getStringAttribute("javaType"))));
        // <1> 获得 extends 属性
        String extend = resultMapNode.getStringAttribute("extends");
        // <1> 获得 autoMapping 属性
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
        // <1> 解析 type 对应的类
        Class<?> typeClass = resolveClass(type);
        Discriminator discriminator = null;
        // <2> 创建 ResultMapping 集合
        List<ResultMapping> resultMappings = new ArrayList<>();
        resultMappings.addAll(additionalResultMappings);
        // <2> 遍历 <resultMap /> 的子节点
        List<XNode> resultChildren = resultMapNode.getChildren();
        for (XNode resultChild : resultChildren) {
            // <2.1> 处理 <constructor /> 节点
            if ("constructor".equals(resultChild.getName())) {
                processConstructorElement(resultChild, typeClass, resultMappings);
                // <2.2> 处理 <discriminator /> 节点
            } else if ("discriminator".equals(resultChild.getName())) {
                discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
                // <2.3> 处理其它节点
            } else {
                List<ResultFlag> flags = new ArrayList<>();
                if ("id".equals(resultChild.getName())) {
                    flags.add(ResultFlag.ID);
                }
                resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
            }
        }
        // <3> 创建 ResultMapResolver 对象，执行解析
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
        try {
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {
            // <4> 解析失败，添加到 configuration 中
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }

    private void processConstructorElement(XNode resultChild, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        List<XNode> argChildren = resultChild.getChildren();
        for (XNode argChild : argChildren) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            flags.add(ResultFlag.CONSTRUCTOR);
            if ("idArg".equals(argChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
        }
    }

    private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String typeHandler = context.getStringAttribute("typeHandler");
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        Map<String, String> discriminatorMap = new HashMap<String, String>();
        for (XNode caseChild : context.getChildren()) {
            String value = caseChild.getStringAttribute("value");
            String resultMap = caseChild.getStringAttribute("resultMap", processNestedResultMappings(caseChild, resultMappings));
            discriminatorMap.put(value, resultMap);
        }
        return builderAssistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
    }

    private void sqlElement(List<XNode> list) throws Exception {
        if (configuration.getDatabaseId() != null) {
            sqlElement(list, configuration.getDatabaseId());
        }
        sqlElement(list, null);
    }

    private void sqlElement(List<XNode> list, String requiredDatabaseId) throws Exception {
        for (XNode context : list) {
            String databaseId = context.getStringAttribute("databaseId");
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
                sqlFragments.put(id, context);
            }
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
            // skip this fragment if there is a previous one with a not null databaseId
            if (this.sqlFragments.containsKey(id)) {
                XNode context = this.sqlFragments.get(id);
                if (context.getStringAttribute("databaseId") != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property = context.getStringAttribute("property");
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String nestedSelect = context.getStringAttribute("select");
        String nestedResultMap = context.getStringAttribute("resultMap",
                processNestedResultMappings(context, Collections.<ResultMapping> emptyList()));
        String notNullColumn = context.getStringAttribute("notNullColumn");
        String columnPrefix = context.getStringAttribute("columnPrefix");
        String typeHandler = context.getStringAttribute("typeHandler");
        String resultSet = context.getStringAttribute("resultSet");
        String foreignColumn = context.getStringAttribute("foreignColumn");
        boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
    }

    private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings) throws Exception {
        if ("association".equals(context.getName())
                || "collection".equals(context.getName())
                || "case".equals(context.getName())) {
            if (context.getStringAttribute("select") == null) {
                ResultMap resultMap = resultMapElement(context, resultMappings);
                return resultMap.getId();
            }
        }
        return null;
    }

    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }
            if (boundType != null) {
                if (!configuration.hasMapper(boundType)) {
                    // Spring may not know the real resource name so we set a flag
                    // to prevent loading again this resource from the mapper interface
                    // look at MapperAnnotationBuilder#loadXmlResource
                    configuration.addLoadedResource("namespace:" + namespace);
                    configuration.addMapper(boundType);
                }
            }
        }
    }

}
