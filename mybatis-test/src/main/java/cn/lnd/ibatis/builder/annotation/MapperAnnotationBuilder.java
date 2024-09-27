package cn.lnd.ibatis.builder.annotation;

import cn.lnd.ibatis.annotations.*;
import cn.lnd.ibatis.mapping.*;
import cn.lnd.ibatis.binding.BindingException;
import cn.lnd.ibatis.binding.MapperMethod;
import cn.lnd.ibatis.builder.BuilderException;
import cn.lnd.ibatis.builder.CacheRefResolver;
import cn.lnd.ibatis.builder.IncompleteElementException;
import cn.lnd.ibatis.builder.MapperBuilderAssistant;
import cn.lnd.ibatis.builder.annotation.MethodResolver;
import cn.lnd.ibatis.builder.annotation.ProviderSqlSource;
import cn.lnd.ibatis.builder.xml.XMLMapperBuilder;
import cn.lnd.ibatis.cursor.Cursor;
import cn.lnd.ibatis.executor.keygen.Jdbc3KeyGenerator;
import cn.lnd.ibatis.executor.keygen.KeyGenerator;
import cn.lnd.ibatis.executor.keygen.NoKeyGenerator;
import cn.lnd.ibatis.executor.keygen.SelectKeyGenerator;
import cn.lnd.ibatis.io.Resources;
import cn.lnd.ibatis.mapping.MappedStatement;
import cn.lnd.ibatis.mapping.ResultMap;
import cn.lnd.ibatis.parsing.PropertyParser;
import cn.lnd.ibatis.reflection.TypeParameterResolver;
import cn.lnd.ibatis.scripting.LanguageDriver;
import cn.lnd.ibatis.session.Configuration;
import cn.lnd.ibatis.session.ResultHandler;
import cn.lnd.ibatis.session.RowBounds;
import cn.lnd.ibatis.type.JdbcType;
import cn.lnd.ibatis.type.TypeHandler;
import cn.lnd.ibatis.type.UnknownTypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 16:50
 */
public class MapperAnnotationBuilder {

    private static final Set<Class<? extends Annotation>> SQL_ANNOTATION_TYPES = new HashSet<>();
    private static final Set<Class<? extends Annotation>> SQL_PROVIDER_ANNOTATION_TYPES = new HashSet<>();

    private final cn.lnd.ibatis.session.Configuration configuration;
    private final cn.lnd.ibatis.builder.MapperBuilderAssistant assistant;
    private final Class<?> type;

    static {
        SQL_ANNOTATION_TYPES.add(Select.class);
        SQL_ANNOTATION_TYPES.add(Insert.class);
        SQL_ANNOTATION_TYPES.add(Update.class);
        SQL_ANNOTATION_TYPES.add(Delete.class);

        SQL_PROVIDER_ANNOTATION_TYPES.add(SelectProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(InsertProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(UpdateProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(DeleteProvider.class);
    }

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace('.', '/') + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
    }

    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            loadXmlResource();
            configuration.addLoadedResource(resource);
            assistant.setCurrentNamespace(type.getName());
            parseCache();
            parseCacheRef();
            Method[] methods = type.getMethods();
            for (Method method : methods) {
                try {
                    // issue #237
                    if (!method.isBridge()) {
                        parseStatement(method);
                    }
                } catch (cn.lnd.ibatis.builder.IncompleteElementException e) {
                    configuration.addIncompleteMethod(new cn.lnd.ibatis.builder.annotation.MethodResolver(this, method));
                }
            }
        }
        parsePendingMethods();
    }

    private void parsePendingMethods() {
        Collection<cn.lnd.ibatis.builder.annotation.MethodResolver> incompleteMethods = configuration.getIncompleteMethods();
        synchronized (incompleteMethods) {
            Iterator<MethodResolver> iter = incompleteMethods.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (cn.lnd.ibatis.builder.IncompleteElementException e) {
                    // This method is still missing a resource
                }
            }
        }
    }

    private void loadXmlResource() {
        // Spring may not know the real resource name so we check a flag
        // to prevent loading again a resource twice
        // this flag is set at XMLMapperBuilder#bindMapperForNamespace
        if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
            String xmlResource = type.getName().replace('.', '/') + ".xml";
            // #1347
            InputStream inputStream = type.getResourceAsStream("/" + xmlResource);
            if (inputStream == null) {
                // Search XML mapper that is not in the module but in the classpath.
                try {
                    inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
                } catch (IOException e2) {
                    // ignore, resource is not required
                }
            }
            if (inputStream != null) {
                cn.lnd.ibatis.builder.xml.XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
                xmlParser.parse();
            }
        }
    }

    private void parseCache() {
        CacheNamespace cacheDomain = type.getAnnotation(CacheNamespace.class);
        if (cacheDomain != null) {
            Integer size = cacheDomain.size() == 0 ? null : cacheDomain.size();
            Long flushInterval = cacheDomain.flushInterval() == 0 ? null : cacheDomain.flushInterval();
            Properties props = convertToProperties(cacheDomain.properties());
            assistant.useNewCache(cacheDomain.implementation(), cacheDomain.eviction(), flushInterval, size, cacheDomain.readWrite(), cacheDomain.blocking(), props);
        }
    }

    private Properties convertToProperties(Property[] properties) {
        if (properties.length == 0) {
            return null;
        }
        Properties props = new Properties();
        for (Property property : properties) {
            props.setProperty(property.name(),
                    PropertyParser.parse(property.value(), configuration.getVariables()));
        }
        return props;
    }

    private void parseCacheRef() {
        CacheNamespaceRef cacheDomainRef = type.getAnnotation(CacheNamespaceRef.class);
        if (cacheDomainRef != null) {
            Class<?> refType = cacheDomainRef.value();
            String refName = cacheDomainRef.name();
            if (refType == void.class && refName.isEmpty()) {
                throw new cn.lnd.ibatis.builder.BuilderException("Should be specified either value() or name() attribute in the @CacheNamespaceRef");
            }
            if (refType != void.class && !refName.isEmpty()) {
                throw new cn.lnd.ibatis.builder.BuilderException("Cannot use both value() and name() attribute in the @CacheNamespaceRef");
            }
            String namespace = (refType != void.class) ? refType.getName() : refName;
            try {
                assistant.useCacheRef(namespace);
            } catch (IncompleteElementException e) {
                configuration.addIncompleteCacheRef(new CacheRefResolver(assistant, namespace));
            }
        }
    }

    private String parseResultMap(Method method) {
        Class<?> returnType = getReturnType(method);
        ConstructorArgs args = method.getAnnotation(ConstructorArgs.class);
        Results results = method.getAnnotation(Results.class);
        TypeDiscriminator typeDiscriminator = method.getAnnotation(TypeDiscriminator.class);
        String resultMapId = generateResultMapName(method);
        applyResultMap(resultMapId, returnType, argsIf(args), resultsIf(results), typeDiscriminator);
        return resultMapId;
    }

    private String generateResultMapName(Method method) {
        Results results = method.getAnnotation(Results.class);
        if (results != null && !results.id().isEmpty()) {
            return type.getName() + "." + results.id();
        }
        StringBuilder suffix = new StringBuilder();
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        return type.getName() + "." + method.getName() + suffix;
    }

    private void applyResultMap(String resultMapId, Class<?> returnType, Arg[] args, Result[] results, TypeDiscriminator discriminator) {
        List<cn.lnd.ibatis.mapping.ResultMapping> resultMappings = new ArrayList<>();
        applyConstructorArgs(args, returnType, resultMappings);
        applyResults(results, returnType, resultMappings);
        cn.lnd.ibatis.mapping.Discriminator disc = applyDiscriminator(resultMapId, returnType, discriminator);
        // TODO add AutoMappingBehaviour
        assistant.addResultMap(resultMapId, returnType, null, disc, resultMappings, null);
        createDiscriminatorResultMaps(resultMapId, returnType, discriminator);
    }

    private void createDiscriminatorResultMaps(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator != null) {
            for (Case c : discriminator.cases()) {
                String caseResultMapId = resultMapId + "-" + c.value();
                List<cn.lnd.ibatis.mapping.ResultMapping> resultMappings = new ArrayList<>();
                // issue #136
                applyConstructorArgs(c.constructArgs(), resultType, resultMappings);
                applyResults(c.results(), resultType, resultMappings);
                // TODO add AutoMappingBehaviour
                assistant.addResultMap(caseResultMapId, c.type(), resultMapId, null, resultMappings, null);
            }
        }
    }

    private cn.lnd.ibatis.mapping.Discriminator applyDiscriminator(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator != null) {
            String column = discriminator.column();
            Class<?> javaType = discriminator.javaType() == void.class ? String.class : discriminator.javaType();
            cn.lnd.ibatis.type.JdbcType jdbcType = discriminator.jdbcType() == cn.lnd.ibatis.type.JdbcType.UNDEFINED ? null : discriminator.jdbcType();
            @SuppressWarnings("unchecked")
            Class<? extends cn.lnd.ibatis.type.TypeHandler<?>> typeHandler = (Class<? extends cn.lnd.ibatis.type.TypeHandler<?>>)
                    (discriminator.typeHandler() == cn.lnd.ibatis.type.UnknownTypeHandler.class ? null : discriminator.typeHandler());
            Case[] cases = discriminator.cases();
            Map<String, String> discriminatorMap = new HashMap<>();
            for (Case c : cases) {
                String value = c.value();
                String caseResultMapId = resultMapId + "-" + value;
                discriminatorMap.put(value, caseResultMapId);
            }
            return assistant.buildDiscriminator(resultType, column, javaType, jdbcType, typeHandler, discriminatorMap);
        }
        return null;
    }

    void parseStatement(Method method) {
        Class<?> parameterTypeClass = getParameterType(method);
        cn.lnd.ibatis.scripting.LanguageDriver languageDriver = getLanguageDriver(method);
        cn.lnd.ibatis.mapping.SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);
        if (sqlSource != null) {
            Options options = method.getAnnotation(Options.class);
            final String mappedStatementId = type.getName() + "." + method.getName();
            Integer fetchSize = null;
            Integer timeout = null;
            cn.lnd.ibatis.mapping.StatementType statementType = cn.lnd.ibatis.mapping.StatementType.PREPARED;
            cn.lnd.ibatis.mapping.ResultSetType resultSetType = null;
            cn.lnd.ibatis.mapping.SqlCommandType sqlCommandType = getSqlCommandType(method);
            boolean isSelect = sqlCommandType == cn.lnd.ibatis.mapping.SqlCommandType.SELECT;
            boolean flushCache = !isSelect;
            boolean useCache = isSelect;

            cn.lnd.ibatis.executor.keygen.KeyGenerator keyGenerator;
            String keyProperty = null;
            String keyColumn = null;
            if (cn.lnd.ibatis.mapping.SqlCommandType.INSERT.equals(sqlCommandType) || cn.lnd.ibatis.mapping.SqlCommandType.UPDATE.equals(sqlCommandType)) {
                // first check for SelectKey annotation - that overrides everything else
                SelectKey selectKey = method.getAnnotation(SelectKey.class);
                if (selectKey != null) {
                    keyGenerator = handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method), languageDriver);
                    keyProperty = selectKey.keyProperty();
                } else if (options == null) {
                    keyGenerator = configuration.isUseGeneratedKeys() ? cn.lnd.ibatis.executor.keygen.Jdbc3KeyGenerator.INSTANCE : cn.lnd.ibatis.executor.keygen.NoKeyGenerator.INSTANCE;
                } else {
                    keyGenerator = options.useGeneratedKeys() ? Jdbc3KeyGenerator.INSTANCE : cn.lnd.ibatis.executor.keygen.NoKeyGenerator.INSTANCE;
                    keyProperty = options.keyProperty();
                    keyColumn = options.keyColumn();
                }
            } else {
                keyGenerator = cn.lnd.ibatis.executor.keygen.NoKeyGenerator.INSTANCE;
            }

            if (options != null) {
                if (Options.FlushCachePolicy.TRUE.equals(options.flushCache())) {
                    flushCache = true;
                } else if (Options.FlushCachePolicy.FALSE.equals(options.flushCache())) {
                    flushCache = false;
                }
                useCache = options.useCache();
                fetchSize = options.fetchSize() > -1 || options.fetchSize() == Integer.MIN_VALUE ? options.fetchSize() : null; //issue #348
                timeout = options.timeout() > -1 ? options.timeout() : null;
                statementType = options.statementType();
                resultSetType = options.resultSetType();
            }

            String resultMapId = null;
            ResultMap resultMapAnnotation = method.getAnnotation(ResultMap.class);
            if (resultMapAnnotation != null) {
                String[] resultMaps = resultMapAnnotation.value();
                StringBuilder sb = new StringBuilder();
                for (String resultMap : resultMaps) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(resultMap);
                }
                resultMapId = sb.toString();
            } else if (isSelect) {
                resultMapId = parseResultMap(method);
            }

            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    statementType,
                    sqlCommandType,
                    fetchSize,
                    timeout,
                    // ParameterMapID
                    null,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    resultSetType,
                    flushCache,
                    useCache,
                    // TODO gcode issue #577
                    false,
                    keyGenerator,
                    keyProperty,
                    keyColumn,
                    // DatabaseID
                    null,
                    languageDriver,
                    // ResultSets
                    options != null ? nullOrEmpty(options.resultSets()) : null);
        }
    }

    private cn.lnd.ibatis.scripting.LanguageDriver getLanguageDriver(Method method) {
        Lang lang = method.getAnnotation(Lang.class);
        Class<? extends cn.lnd.ibatis.scripting.LanguageDriver> langClass = null;
        if (lang != null) {
            langClass = lang.value();
        }
        return assistant.getLanguageDriver(langClass);
    }

    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> currentParameterType : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(currentParameterType) && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
                if (parameterType == null) {
                    parameterType = currentParameterType;
                } else {
                    // issue #135
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            // gcode issue #508
            if (void.class.equals(returnType)) {
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) resolvedReturnType;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // (gcode issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                // (gcode issue 504) Do not look into Maps if there is not MapKey annotation
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue 443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            } else if (Optional.class.equals(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                Type returnTypeParameter = actualTypeArguments[0];
                if (returnTypeParameter instanceof Class<?>) {
                    returnType = (Class<?>) returnTypeParameter;
                }
            }
        }

        return returnType;
    }

    private cn.lnd.ibatis.mapping.SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, cn.lnd.ibatis.scripting.LanguageDriver languageDriver) {
        try {
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            Class<? extends Annotation> sqlProviderAnnotationType = getSqlProviderAnnotationType(method);
            if (sqlAnnotationType != null) {
                if (sqlProviderAnnotationType != null) {
                    throw new BindingException("You cannot supply both a static SQL and SqlProvider to method named " + method.getName());
                }
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            } else if (sqlProviderAnnotationType != null) {
                Annotation sqlProviderAnnotation = method.getAnnotation(sqlProviderAnnotationType);
                return new ProviderSqlSource(assistant.getConfiguration(), sqlProviderAnnotation, type, method);
            }
            return null;
        } catch (Exception e) {
            throw new cn.lnd.ibatis.builder.BuilderException("Could not find value method on SQL annotation.  Cause: " + e, e);
        }
    }

    private cn.lnd.ibatis.mapping.SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, cn.lnd.ibatis.scripting.LanguageDriver languageDriver) {
        final StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }
        return languageDriver.createSqlSource(configuration, sql.toString().trim(), parameterTypeClass);
    }

    private cn.lnd.ibatis.mapping.SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = getSqlAnnotationType(method);

        if (type == null) {
            type = getSqlProviderAnnotationType(method);

            if (type == null) {
                return cn.lnd.ibatis.mapping.SqlCommandType.UNKNOWN;
            }

            if (type == SelectProvider.class) {
                type = Select.class;
            } else if (type == InsertProvider.class) {
                type = Insert.class;
            } else if (type == UpdateProvider.class) {
                type = Update.class;
            } else if (type == DeleteProvider.class) {
                type = Delete.class;
            }
        }

        return cn.lnd.ibatis.mapping.SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        return chooseAnnotationType(method, SQL_ANNOTATION_TYPES);
    }

    private Class<? extends Annotation> getSqlProviderAnnotationType(Method method) {
        return chooseAnnotationType(method, SQL_PROVIDER_ANNOTATION_TYPES);
    }

    private Class<? extends Annotation> chooseAnnotationType(Method method, Set<Class<? extends Annotation>> types) {
        for (Class<? extends Annotation> type : types) {
            Annotation annotation = method.getAnnotation(type);
            if (annotation != null) {
                return type;
            }
        }
        return null;
    }

    private void applyResults(Result[] results, Class<?> resultType, List<cn.lnd.ibatis.mapping.ResultMapping> resultMappings) {
        for (Result result : results) {
            List<cn.lnd.ibatis.mapping.ResultFlag> flags = new ArrayList<>();
            if (result.id()) {
                flags.add(cn.lnd.ibatis.mapping.ResultFlag.ID);
            }
            @SuppressWarnings("unchecked")
            Class<? extends cn.lnd.ibatis.type.TypeHandler<?>> typeHandler = (Class<? extends cn.lnd.ibatis.type.TypeHandler<?>>)
                    ((result.typeHandler() == cn.lnd.ibatis.type.UnknownTypeHandler.class) ? null : result.typeHandler());
            cn.lnd.ibatis.mapping.ResultMapping resultMapping = assistant.buildResultMapping(
                    resultType,
                    nullOrEmpty(result.property()),
                    nullOrEmpty(result.column()),
                    result.javaType() == void.class ? null : result.javaType(),
                    result.jdbcType() == cn.lnd.ibatis.type.JdbcType.UNDEFINED ? null : result.jdbcType(),
                    hasNestedSelect(result) ? nestedSelectId(result) : null,
                    null,
                    null,
                    null,
                    typeHandler,
                    flags,
                    null,
                    null,
                    isLazy(result));
            resultMappings.add(resultMapping);
        }
    }

    private String nestedSelectId(Result result) {
        String nestedSelect = result.one().select();
        if (nestedSelect.length() < 1) {
            nestedSelect = result.many().select();
        }
        if (!nestedSelect.contains(".")) {
            nestedSelect = type.getName() + "." + nestedSelect;
        }
        return nestedSelect;
    }

    private boolean isLazy(Result result) {
        boolean isLazy = configuration.isLazyLoadingEnabled();
        if (result.one().select().length() > 0 && cn.lnd.ibatis.mapping.FetchType.DEFAULT != result.one().fetchType()) {
            isLazy = result.one().fetchType() == cn.lnd.ibatis.mapping.FetchType.LAZY;
        } else if (result.many().select().length() > 0 && cn.lnd.ibatis.mapping.FetchType.DEFAULT != result.many().fetchType()) {
            isLazy = result.many().fetchType() == cn.lnd.ibatis.mapping.FetchType.LAZY;
        }
        return isLazy;
    }

    private boolean hasNestedSelect(Result result) {
        if (result.one().select().length() > 0 && result.many().select().length() > 0) {
            throw new BuilderException("Cannot use both @One and @Many annotations in the same @Result");
        }
        return result.one().select().length() > 0 || result.many().select().length() > 0;
    }

    private void applyConstructorArgs(Arg[] args, Class<?> resultType, List<cn.lnd.ibatis.mapping.ResultMapping> resultMappings) {
        for (Arg arg : args) {
            List<cn.lnd.ibatis.mapping.ResultFlag> flags = new ArrayList<>();
            flags.add(cn.lnd.ibatis.mapping.ResultFlag.CONSTRUCTOR);
            if (arg.id()) {
                flags.add(cn.lnd.ibatis.mapping.ResultFlag.ID);
            }
            @SuppressWarnings("unchecked")
            Class<? extends cn.lnd.ibatis.type.TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
                    (arg.typeHandler() == UnknownTypeHandler.class ? null : arg.typeHandler());
            cn.lnd.ibatis.mapping.ResultMapping resultMapping = assistant.buildResultMapping(
                    resultType,
                    nullOrEmpty(arg.name()),
                    nullOrEmpty(arg.column()),
                    arg.javaType() == void.class ? null : arg.javaType(),
                    arg.jdbcType() == JdbcType.UNDEFINED ? null : arg.jdbcType(),
                    nullOrEmpty(arg.select()),
                    nullOrEmpty(arg.resultMap()),
                    null,
                    nullOrEmpty(arg.columnPrefix()),
                    typeHandler,
                    flags,
                    null,
                    null,
                    false);
            resultMappings.add(resultMapping);
        }
    }

    private String nullOrEmpty(String value) {
        return value == null || value.trim().length() == 0 ? null : value;
    }

    private Result[] resultsIf(Results results) {
        return results == null ? new Result[0] : results.value();
    }

    private Arg[] argsIf(ConstructorArgs args) {
        return args == null ? new Arg[0] : args.value();
    }

    private cn.lnd.ibatis.executor.keygen.KeyGenerator handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        String id = baseStatementId + cn.lnd.ibatis.executor.keygen.SelectKeyGenerator.SELECT_KEY_SUFFIX;
        Class<?> resultTypeClass = selectKeyAnnotation.resultType();
        cn.lnd.ibatis.mapping.StatementType statementType = selectKeyAnnotation.statementType();
        String keyProperty = selectKeyAnnotation.keyProperty();
        String keyColumn = selectKeyAnnotation.keyColumn();
        boolean executeBefore = selectKeyAnnotation.before();

        // defaults
        boolean useCache = false;
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        Integer fetchSize = null;
        Integer timeout = null;
        boolean flushCache = false;
        String parameterMap = null;
        String resultMap = null;
        cn.lnd.ibatis.mapping.ResultSetType resultSetTypeEnum = null;

        cn.lnd.ibatis.mapping.SqlSource sqlSource = buildSqlSourceFromStrings(selectKeyAnnotation.statement(), parameterTypeClass, languageDriver);
        cn.lnd.ibatis.mapping.SqlCommandType sqlCommandType = cn.lnd.ibatis.mapping.SqlCommandType.SELECT;

        assistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass, resultSetTypeEnum,
                flushCache, useCache, false,
                keyGenerator, keyProperty, keyColumn, null, languageDriver, null);

        id = assistant.applyCurrentNamespace(id, false);

        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
        cn.lnd.ibatis.executor.keygen.SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, executeBefore);
        configuration.addKeyGenerator(id, answer);
        return answer;
    }

}
