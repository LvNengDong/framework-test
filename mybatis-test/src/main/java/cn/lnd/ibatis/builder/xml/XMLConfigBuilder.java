package cn.lnd.ibatis.builder.xml;

import cn.lnd.ibatis.builder.BaseBuilder;
import cn.lnd.ibatis.builder.BuilderException;
import cn.lnd.ibatis.datasource.DataSourceFactory;
import cn.lnd.ibatis.executor.ErrorContext;
import cn.lnd.ibatis.executor.loader.ProxyFactory;
import cn.lnd.ibatis.io.Resources;
import cn.lnd.ibatis.io.VFS;
import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.mapping.DatabaseIdProvider;
import cn.lnd.ibatis.mapping.Environment;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.parsing.XPathParser;
import cn.lnd.ibatis.plugin.Interceptor;
import cn.lnd.ibatis.reflection.DefaultReflectorFactory;
import cn.lnd.ibatis.reflection.MetaClass;
import cn.lnd.ibatis.reflection.ReflectorFactory;
import cn.lnd.ibatis.reflection.factory.ObjectFactory;
import cn.lnd.ibatis.reflection.wrapper.ObjectWrapperFactory;
import cn.lnd.ibatis.session.*;
import cn.lnd.ibatis.transaction.TransactionFactory;
import cn.lnd.ibatis.type.JdbcType;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * @Author lnd
 * @Description
 *      XML 配置构建器
 *      主要功能：
 *          1、构造函数：在构造函数中，调用 XPath 解析器加载 mybatis-config.xml 配置文件到内存中
 *          2、parse() 方法：负责解析 mybatis-config.xml 配置文件，得到对应的 Configuration 全局配置对象。
 * @Date 2024/9/18 23:38
 */
public class XMLConfigBuilder extends BaseBuilder {

    /* 是否已解析 */
    private boolean parsed;
    /* XML 解析器 */
    private final XPathParser parser;
    /* 环境 (pord、dev、test等等)*/
    private String environment;
    /* ReflectorFactory 对象 (主要用于操作 Reflector 对象的创建和缓存）*/
    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public XMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }

    public XMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }

    public XMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(
                new XPathParser(reader, true, props, new XMLMapperEntityResolver()),
                environment,
                props
        );
    }

    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        // <1> 创建 Configuration 对象
        super(new Configuration());
        ErrorContext.instance().resource("SQL Mapper Configuration");
        // <2> 设置 Configuration 的 variables 属性
        this.configuration.setVariables(props);
        this.parsed = false;
        this.environment = environment;
        this.parser = parser;
    }

    /**
     * 解析 XML 成 Configuration 对象
     * @return
     */
    public Configuration parse() {
        // <1.1> 若已解析，抛出 BuilderException 异常
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        // <1.2> 标记已解析
        parsed = true;
        // <2> 解析 XML configuration 节点（configuration节点就是根节点）
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            //issue #117 read properties first
            // <1> 解析 <properties /> 标签
            propertiesElement(root.evalNode("properties"));
            // <2> 解析 <settings /> 标签
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            // <3> 加载自定义的 VFS 实现类
            loadCustomVfs(settings);
            // <4> 解析 <typeAliases /> 标签
            typeAliasesElement(root.evalNode("typeAliases"));
            // <5> 解析 <plugins /> 标签
            pluginElement(root.evalNode("plugins"));
            // <6> 解析 <objectFactory /> 标签
            objectFactoryElement(root.evalNode("objectFactory"));
            // <7> 解析 <objectWrapperFactory /> 标签
            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
            // <8> 解析 <reflectorFactory /> 标签
            reflectorFactoryElement(root.evalNode("reflectorFactory"));
            // <9> 赋值 <settings /> 到 Configuration 属性
            settingsElement(settings);
            // read it after objectFactory and objectWrapperFactory issue #631
            // <10> 解析 <environments /> 标签
            environmentsElement(root.evalNode("environments"));
            // <11> 解析 <databaseIdProvider /> 标签
            databaseIdProviderElement(root.evalNode("databaseIdProvider"));
            // <12> 解析 <typeHandlers /> 标签
            typeHandlerElement(root.evalNode("typeHandlers"));
            // <13> 解析 <mappers /> 标签
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    /*
    * 处理<settings>标签的所有子标签，也就是<setting>标签，将其name属性和value属性整理到Properties对象中保存
    * */
    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        // 将子标签，解析成 Properties 对象
        Properties props = context.getChildrenAsProperties();
        // Check that all settings are known to the configuration class
        // 校验每个属性，在 Configuration 中，有相应的 setting 方法，否则抛出 BuilderException 异常
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    private void loadCustomVfs(Properties props) throws ClassNotFoundException {
        // 获得 vfsImpl 属性
        String value = props.getProperty("vfsImpl");
        if (value != null) {
            // 使用 , 作为分隔符，拆成 VFS 类名的数组
            String[] clazzes = value.split(",");
            // 遍历 VFS 类名的数组
            for (String clazz : clazzes) {
                if (!clazz.isEmpty()) {
                    // 获得 VFS 类
                    @SuppressWarnings("unchecked")
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
                    // 设置到 Configuration 中
                    configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    /**
     * 处理 <typeAliases> 标签，解析得到的别名信息会记录到 TypeAliasRegistry 中
     */
    private void typeAliasesElement(XNode parent) {
        if (parent != null) {
            // 遍历子节点
            for (XNode child : parent.getChildren()) {
                // 指定为包的情况下，注册包下的每个类
                if ("package".equals(child.getName())) {
                    String typeAliasPackage = child.getStringAttribute("name");
                    configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
                }
                // 指定为类的情况下，直接注册类和别名
                else {
                    String alias = child.getStringAttribute("alias");
                    String type = child.getStringAttribute("type");
                    try {
                        Class<?> clazz = Resources.classForName(type); // 获得类是否存在
                        // 注册到 typeAliasRegistry 中
                        if (alias == null) {
                            typeAliasRegistry.registerAlias(clazz);
                        } else {
                            typeAliasRegistry.registerAlias(alias, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                    }
                }
            }
        }
    }

    /**
     *    MyBatis 是一个非常易于扩展的持久层框架，而插件就是 MyBatis 提供的一种重要扩展机制。
     *    我们可以自定义一个实现了 Interceptor 接口的插件来扩展 MyBatis 的行为，或是拦截 MyBatis 的一些默认行为。
     * */
    private void pluginElement(XNode parent) throws Exception {
        if (parent != null) {
            // 遍历 <plugins /> 标签
            for (XNode child : parent.getChildren()) {
                String interceptor = child.getStringAttribute("interceptor");
                Properties properties = child.getChildrenAsProperties();
                // <1> 创建 Interceptor 对象，并设置属性
                Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
                interceptorInstance.setProperties(properties);
                // <2> 添加到 configuration 中
                configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    private void objectFactoryElement(XNode context) throws Exception {
        if (context != null) {
            // 获得 ObjectFactory 的实现类
            String type = context.getStringAttribute("type");
            // 获得 Properties 属性
            Properties properties = context.getChildrenAsProperties();
            // <1> 创建 ObjectFactory 对象，并设置 Properties 属性
            ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();
            factory.setProperties(properties);
            // <2> 设置 Configuration 的 objectFactory 属性
            configuration.setObjectFactory(factory);
        }
    }

    private void objectWrapperFactoryElement(XNode context) throws Exception {
        if (context != null) {
            // 获得 objectWrapperFactory 的实现类
            String type = context.getStringAttribute("type");
            // <1> 创建 ObjectWrapperFactory 对象
            ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).newInstance();
            // 设置 Configuration 的 objectWrapperFactory 属性
            configuration.setObjectWrapperFactory(factory);
        }
    }

    private void reflectorFactoryElement(XNode context) throws Exception {
        if (context != null) {
            // 获得 ReflectorFactory 的实现类
            String type = context.getStringAttribute("type");
            // 创建 ReflectorFactory 对象
            ReflectorFactory factory = (ReflectorFactory) resolveClass(type).newInstance();
            // 设置 Configuration 的 reflectorFactory 属性
            configuration.setReflectorFactory(factory);
        }
    }

    /**
     * 如果一个属性在不只一个地方进行了配置，那么，MyBatis 将按照下面的顺序来加载：
     *   1、首先读取在 properties 元素体内指定的属性。
     *   2、然后根据 properties 元素中的 resource 属性读取类路径下属性文件，或根据 url 属性指定的路径读取属性文件，并覆盖之前读取过的同名属性。（resource 和 url 是互斥的，所以不存在先后顺序）
     *   3、最后读取作为方法参数传递的属性，并覆盖之前读取过的同名属性。
     * 因此，通过方法参数传递的属性具有最高优先级，resource/url 属性中指定的配置文件次之，最低优先级的则是 properties 元素中指定的属性。
     */
    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            // 读取子标签们，为 Properties 对象
            Properties defaults = context.getChildrenAsProperties();
            // 读取 resource 和 url 属性
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            if (resource != null && url != null) { // resource 和 url 都存在的情况下，抛出 BuilderException 异常
                throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            // 读取本地 Properties 配置文件到 defaults 中。
            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            }
            // 读取远程 Properties 配置文件到 defaults 中。
            else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            // 覆盖 configuration 中的 Properties 对象到 defaults 中。
            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            // 设置 defaults 到 parser 和 configuration 中。
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    /**
     * 赋值 <settings /> 到 Configuration 属性
     * @param props
     * @throws Exception
     */
    private void settingsElement(Properties props) throws Exception {
        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
        configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
        configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
        configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
        configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
        configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
        configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
        configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
        configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
        configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
        configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
        configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
        configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
        configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        configuration.setLogPrefix(props.getProperty("logPrefix"));
        @SuppressWarnings("unchecked")
        Class<? extends Log> logImpl = (Class<? extends Log>)resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);
        configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
    }

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            // <1> environment 属性为空，从 default 属性获得
            if (environment == null) {
                environment = context.getStringAttribute("default");
            }
            // 遍历 XNode 节点
            for (XNode child : context.getChildren()) {
                // <2> 判断 environment 是否匹配
                String id = child.getStringAttribute("id");
                if (isSpecifiedEnvironment(id)) {
                    // <3> 解析 `<transactionManager />` 标签，返回 TransactionFactory 对象
                    TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                    // <4> 解析 `<dataSource />` 标签，返回 DataSourceFactory 对象
                    DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    DataSource dataSource = dsFactory.getDataSource();
                    // <5> 创建 Environment.Builder 对象
                    Environment.Builder environmentBuilder = new Environment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);
                    // <6> 构造 Environment 对象，并设置到 configuration 中
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    /**
     * 解析 <databaseIdProvider /> 标签
     */
    private void databaseIdProviderElement(XNode context) throws Exception {
        DatabaseIdProvider databaseIdProvider = null;
        if (context != null) {
            // <1> 获得 DatabaseIdProvider 的类
            String type = context.getStringAttribute("type");
            // awful patch to keep backward compatibility  保持兼容
            if ("VENDOR".equals(type)) {
                type = "DB_VENDOR";
            }
            // <2> 获得 Properties 对象
            Properties properties = context.getChildrenAsProperties();
            // <3> 创建 DatabaseIdProvider 对象，并设置对应的属性
            databaseIdProvider = (DatabaseIdProvider) resolveClass(type).newInstance();
            databaseIdProvider.setProperties(properties);
        }

        Environment environment = configuration.getEnvironment();
        if (environment != null && databaseIdProvider != null) {
            // <4> 获得对应的 databaseId 编号
            String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
            // <5> 设置到 configuration 中
            configuration.setDatabaseId(databaseId);
        }
    }

    /**
     * 解析 <transactionManager/> 标签，返回 TransactionFactory 对象
     */
    private TransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            // 获得 TransactionFactory 的类
            String type = context.getStringAttribute("type");
            // 获得 Properties 属性
            Properties props = context.getChildrenAsProperties();
            // 创建 TransactionFactory 对象，并设置属性
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    /**
     * 解析 <dataSource /> 标签，返回 DataSourceFactory 对象
     * */
    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            // 获得 DataSourceFactory 的类
            String type = context.getStringAttribute("type");
            // 获得 Properties 属性
            Properties props = context.getChildrenAsProperties();
            // 创建 DataSourceFactory 对象，并设置属性
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    /**
     * 解析 <typeHandlers /> 标签
     * */
    private void typeHandlerElement(XNode parent) throws Exception {
        if (parent != null) {
            // 遍历子节点
            for (XNode child : parent.getChildren()) {
                // <1> 如果是 package 标签，则扫描该包，并注册包下的所有 typeHandler 信息
                if ("package".equals(child.getName())) {
                    String typeHandlerPackage = child.getStringAttribute("name");
                    typeHandlerRegistry.register(typeHandlerPackage);
                }
                // <2> 如果是 typeHandler 标签，则注册该 typeHandler 信息
                else {
                    // 获得 javaType、jdbcType、handler
                    String javaTypeName = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");
                    Class<?> javaTypeClass = resolveClass(javaTypeName);
                    JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                    // 注册 typeHandler
                    if (javaTypeClass != null) {
                        if (jdbcType == null) {
                            typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {
                            typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }
                    } else {
                        typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    /**
     * 解析 <mappers /> 标签
     * */
    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            // <0> 遍历子节点
            for (XNode child : parent.getChildren()) {
                // <1> 如果是 package 标签，则扫描该包
                if ("package".equals(child.getName())) {
                    // 获得包名
                    String mapperPackage = child.getStringAttribute("name");
                    // 添加到 configuration 中
                    configuration.addMappers(mapperPackage);
                }
                // 如果是 mapper 标签
                else {
                    // 获得 resource、url、class 属性
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");
                    // <2> 使用相对于类路径的资源引用
                    if (resource != null && url == null && mapperClass == null) {
                        ErrorContext.instance().resource(resource);
                        // 获得 resource 的 InputStream 对象
                        InputStream inputStream = Resources.getResourceAsStream(resource);
                        // 创建 XMLMapperBuilder 对象
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                        // 执行解析
                        mapperParser.parse();
                    }
                    // <3> 使用完全限定资源定位符（URL）
                    else if (resource == null && url != null && mapperClass == null) {
                        ErrorContext.instance().resource(url);
                        // 获得 url 的 InputStream 对象
                        InputStream inputStream = Resources.getUrlAsStream(url);
                        // 创建 XMLMapperBuilder 对象
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                        // 执行解析
                        mapperParser.parse();
                    }
                    // <4> 使用映射器接口实现类的完全限定类名
                    else if (resource == null && url == null && mapperClass != null) {
                        // 获得 Mapper 接口
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        // 添加到 configuration 中
                        configuration.addMapper(mapperInterface);
                    } else {
                        throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                    }
                }
            }
        }
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("No environment specified.");
        } else if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }

}
