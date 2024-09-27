package cn.lnd.ibatis.scripting.xmltags;

import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;
import cn.lnd.ibatis.reflection.MetaObject;
import cn.lnd.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 20:58
 */
public class DynamicContext {

    public static final String PARAMETER_OBJECT_KEY = "_parameter";
    public static final String DATABASE_ID_KEY = "_databaseId";

    static {
        OgnlRuntime.setPropertyAccessor(DynamicContext.ContextMap.class, new DynamicContext.ContextAccessor());
    }

    private final DynamicContext.ContextMap bindings;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private int uniqueNumber = 0;

    public DynamicContext(Configuration configuration, Object parameterObject) {
        if (parameterObject != null && !(parameterObject instanceof Map)) {
            cn.lnd.ibatis.reflection.MetaObject metaObject = configuration.newMetaObject(parameterObject);
            bindings = new DynamicContext.ContextMap(metaObject);
        } else {
            bindings = new DynamicContext.ContextMap(null);
        }
        bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
        bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void bind(String name, Object value) {
        bindings.put(name, value);
    }

    public void appendSql(String sql) {
        sqlBuilder.append(sql);
        sqlBuilder.append(" ");
    }

    public String getSql() {
        return sqlBuilder.toString().trim();
    }

    public int getUniqueNumber() {
        return uniqueNumber++;
    }

    static class ContextMap extends HashMap<String, Object> {
        private static final long serialVersionUID = 2977601501966151582L;

        private cn.lnd.ibatis.reflection.MetaObject parameterMetaObject;
        public ContextMap(MetaObject parameterMetaObject) {
            this.parameterMetaObject = parameterMetaObject;
        }

        @Override
        public Object get(Object key) {
            String strKey = (String) key;
            if (super.containsKey(strKey)) {
                return super.get(strKey);
            }

            if (parameterMetaObject != null) {
                // issue #61 do not modify the context when reading
                return parameterMetaObject.getValue(strKey);
            }

            return null;
        }
    }

    static class ContextAccessor implements PropertyAccessor {

        @Override
        public Object getProperty(Map context, Object target, Object name) {
            Map map = (Map) target;

            Object result = map.get(name);
            if (map.containsKey(name) || result != null) {
                return result;
            }

            Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
            if (parameterObject instanceof Map) {
                return ((Map)parameterObject).get(name);
            }

            return null;
        }

        @Override
        public void setProperty(Map context, Object target, Object name, Object value) {
            Map<Object, Object> map = (Map<Object, Object>) target;
            map.put(name, value);
        }

        @Override
        public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }

        @Override
        public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }
    }
}
