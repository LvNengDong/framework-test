package cn.lnd.ibatis.mapping;

import cn.lnd.ibatis.cache.Cache;
import cn.lnd.ibatis.executor.keygen.Jdbc3KeyGenerator;
import cn.lnd.ibatis.executor.keygen.KeyGenerator;
import cn.lnd.ibatis.executor.keygen.NoKeyGenerator;
import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;
import cn.lnd.ibatis.scripting.LanguageDriver;
import cn.lnd.ibatis.session.Configuration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 11:28
 */
@Getter
public class MappedStatement {

    
    private String resource;
    
    private Configuration configuration;
    
    private String id;
    
    private Integer fetchSize;
    
    private Integer timeout;
    
    private StatementType statementType;
    
    private ResultSetType resultSetType;

    // SQL 标签中定义的 SQL 语句（解析前，即未解析占位符）
    private SqlSource sqlSource;
    
    private Cache cache;
    
    private ParameterMap parameterMap;
    
    private List<ResultMap> resultMaps;
    
    private boolean flushCacheRequired;
    
    private boolean useCache;
    
    private boolean resultOrdered;
    
    
    /*  SQL 语句的类型（INSERT、UPDATE、DELETE、SELECT 或 FLUSH 类型） */
    private SqlCommandType sqlCommandType;
    
    private KeyGenerator keyGenerator;
    
    private String[] keyProperties;
    
    private String[] keyColumns;
    private boolean hasNestedResultMaps;
    
    private String databaseId;
    
    private Log statementLog;
    
    private LanguageDriver lang;
    
    private String[] resultSets;

    MappedStatement() {
        // constructor disabled
    }

    public static class Builder {
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.statementType = StatementType.PREPARED;
            mappedStatement.parameterMap = new ParameterMap.Builder(configuration, "defaultParameterMap", null, new ArrayList<ParameterMapping>()).build();
            mappedStatement.resultMaps = new ArrayList<ResultMap>();
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            String logId = id;
            if (configuration.getLogPrefix() != null) {
                logId = configuration.getLogPrefix() + id;
            }
            mappedStatement.statementLog = LogFactory.getLog(logId);
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public MappedStatement.Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        public String id() {
            return mappedStatement.id;
        }

        public MappedStatement.Builder parameterMap(ParameterMap parameterMap) {
            mappedStatement.parameterMap = parameterMap;
            return this;
        }

        public MappedStatement.Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            for (ResultMap resultMap : resultMaps) {
                mappedStatement.hasNestedResultMaps = mappedStatement.hasNestedResultMaps || resultMap.hasNestedResultMaps();
            }
            return this;
        }

        public MappedStatement.Builder fetchSize(Integer fetchSize) {
            mappedStatement.fetchSize = fetchSize;
            return this;
        }

        public MappedStatement.Builder timeout(Integer timeout) {
            mappedStatement.timeout = timeout;
            return this;
        }

        public MappedStatement.Builder statementType(StatementType statementType) {
            mappedStatement.statementType = statementType;
            return this;
        }

        public MappedStatement.Builder resultSetType(ResultSetType resultSetType) {
            mappedStatement.resultSetType = resultSetType;
            return this;
        }

        public MappedStatement.Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }

        public MappedStatement.Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }

        public MappedStatement.Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }

        public MappedStatement.Builder resultOrdered(boolean resultOrdered) {
            mappedStatement.resultOrdered = resultOrdered;
            return this;
        }

        public MappedStatement.Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        public MappedStatement.Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }

        public MappedStatement.Builder keyColumn(String keyColumn) {
            mappedStatement.keyColumns = delimitedStringToArray(keyColumn);
            return this;
        }

        public MappedStatement.Builder databaseId(String databaseId) {
            mappedStatement.databaseId = databaseId;
            return this;
        }

        public MappedStatement.Builder lang(LanguageDriver driver) {
            mappedStatement.lang = driver;
            return this;
        }

        public MappedStatement.Builder resultSets(String resultSet) {
            mappedStatement.resultSets = delimitedStringToArray(resultSet);
            return this;
        }

        /** @deprecated Use {@link #resultSets} */
        @Deprecated
        public MappedStatement.Builder resulSets(String resultSet) {
            mappedStatement.resultSets = delimitedStringToArray(resultSet);
            return this;
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            assert mappedStatement.sqlSource != null;
            assert mappedStatement.lang != null;
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }
    }

    public boolean hasNestedResultMaps() {
        return hasNestedResultMaps;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    /** @deprecated Use {@link #getResultSets()} */
    @Deprecated
    public String[] getResulSets() {
        return resultSets;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
        }

        // check for nested result maps in parameter mappings (issue #30)
        for (ParameterMapping pm : boundSql.getParameterMappings()) {
            String rmId = pm.getResultMapId();
            if (rmId != null) {
                ResultMap rm = configuration.getResultMap(rmId);
                if (rm != null) {
                    hasNestedResultMaps |= rm.hasNestedResultMaps();
                }
            }
        }

        return boundSql;
    }

    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().isEmpty()) {
            return null;
        } else {
            return in.split(",");
        }
    }

}
