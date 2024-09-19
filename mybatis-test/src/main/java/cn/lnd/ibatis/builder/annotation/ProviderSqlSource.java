package cn.lnd.ibatis.builder.annotation;

import cn.lnd.ibatis.builder.BuilderException;
import cn.lnd.ibatis.builder.SqlSourceBuilder;
import cn.lnd.ibatis.mapping.BoundSql;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.reflection.ParamNameResolver;
import cn.lnd.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 16:51
 */
public class ProviderSqlSource implements SqlSource {

    private SqlSourceBuilder sqlSourceParser;
    private Class<?> providerType;
    private Method providerMethod;
    private String[] providerMethodArgumentNames;

    public ProviderSqlSource(Configuration config, Object provider) {
        String providerMethodName;
        try {
            this.sqlSourceParser = new SqlSourceBuilder(config);
            this.providerType = (Class<?>) provider.getClass().getMethod("type").invoke(provider);
            providerMethodName = (String) provider.getClass().getMethod("method").invoke(provider);

            for (Method m : this.providerType.getMethods()) {
                if (providerMethodName.equals(m.getName())) {
                    if (m.getReturnType() == String.class) {
                        if (providerMethod != null){
                            throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                                    + providerMethodName + "' is found multiple in SqlProvider '" + this.providerType.getName()
                                    + "'. Sql provider method can not overload.");
                        }
                        this.providerMethod = m;
                        this.providerMethodArgumentNames = new ParamNameResolver(config, m).getNames();
                    }
                }
            }
        } catch (BuilderException e) {
            throw e;
        } catch (Exception e) {
            throw new BuilderException("Error creating SqlSource for SqlProvider.  Cause: " + e, e);
        }
        if (this.providerMethod == null) {
            throw new BuilderException("Error creating SqlSource for SqlProvider. Method '"
                    + providerMethodName + "' not found in SqlProvider '" + this.providerType.getName() + "'.");
        }
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        SqlSource sqlSource = createSqlSource(parameterObject);
        return sqlSource.getBoundSql(parameterObject);
    }

    private SqlSource createSqlSource(Object parameterObject) {
        try {
            Class<?>[] parameterTypes = providerMethod.getParameterTypes();
            String sql;
            if (parameterTypes.length == 0) {
                sql = (String) providerMethod.invoke(providerType.newInstance());
            } else if (parameterTypes.length == 1 &&
                    (parameterObject == null || parameterTypes[0].isAssignableFrom(parameterObject.getClass()))) {
                sql = (String) providerMethod.invoke(providerType.newInstance(), parameterObject);
            } else if (parameterObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) parameterObject;
                sql = (String) providerMethod.invoke(providerType.newInstance(), extractProviderMethodArguments(params, providerMethodArgumentNames));
            } else {
                throw new BuilderException("Error invoking SqlProvider method ("
                        + providerType.getName() + "." + providerMethod.getName()
                        + "). Cannot invoke a method that holds "
                        + (parameterTypes.length == 1 ? "named argument(@Param)": "multiple arguments")
                        + " using a specifying parameterObject. In this case, please specify a 'java.util.Map' object.");
            }
            Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
            return sqlSourceParser.parse(sql, parameterType, new HashMap<String, Object>());
        } catch (BuilderException e) {
            throw e;
        } catch (Exception e) {
            throw new BuilderException("Error invoking SqlProvider method ("
                    + providerType.getName() + "." + providerMethod.getName()
                    + ").  Cause: " + e, e);
        }
    }

    private Object[] extractProviderMethodArguments(Map<String, Object> params, String[] argumentNames) {
        Object[] args = new Object[argumentNames.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = params.get(argumentNames[i]);
        }
        return args;
    }

}
