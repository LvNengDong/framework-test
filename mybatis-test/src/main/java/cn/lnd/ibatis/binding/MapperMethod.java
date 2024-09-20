package cn.lnd.ibatis.binding;

import cn.lnd.ibatis.cursor.Cursor;
import cn.lnd.ibatis.mapping.MappedStatement;
import cn.lnd.ibatis.mapping.SqlCommandType;
import cn.lnd.ibatis.reflection.MetaObject;
import cn.lnd.ibatis.session.Configuration;
import cn.lnd.ibatis.session.RowBounds;
import cn.lnd.ibatis.session.SqlSession;
import lombok.Getter;
import cn.lnd.ibatis.annotations.Flush;
import cn.lnd.ibatis.annotations.MapKey;
import cn.lnd.ibatis.reflection.ParamNameResolver;
import cn.lnd.ibatis.reflection.TypeParameterResolver;
import cn.lnd.ibatis.session.ResultHandler;


import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @Author lnd
 * @Description
 *
 *      Mapper 方法。
 *      在 Mapper 接口中，每个定义的方法，对应一个 MapperMethod 对象。
 *
 * @Date 2024/9/19 11:26
 */
public class MapperMethod {

    /** SqlCommand 对象 */
    private final MapperMethod.SqlCommand command;

    /** MethodSignature 对象 */
    private final MapperMethod.MethodSignature method;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new MapperMethod.SqlCommand(config, mapperInterface, method);
        this.method = new MapperMethod.MethodSignature(config, mapperInterface, method);
    }

    /**
     * 因为涉及比较多的后面的内容，所以放在 详细解析。
     * 心急的胖友，可以先看看 《Mybatis3.3.x技术内幕（十一）：执行一个Sql命令的完整流程》 。
     * */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (command.getType()) {
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(command.getName(), param));
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(command.getName(), param));
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(command.getName(), param));
                break;
            }
            case SELECT:
                if (method.returnsVoid() && method.hasResultHandler()) {
                    executeWithResultHandler(sqlSession, args);
                    result = null;
                } else if (method.returnsMany()) {
                    result = executeForMany(sqlSession, args);
                } else if (method.returnsMap()) {
                    result = executeForMap(sqlSession, args);
                } else if (method.returnsCursor()) {
                    result = executeForCursor(sqlSession, args);
                } else {
                    Object param = method.convertArgsToSqlCommandParam(args);
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }
        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    private Object rowCountResult(int rowCount) {
        final Object result;
        if (method.returnsVoid()) {
            result = null;
        } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
            result = rowCount;
        } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
            result = (long)rowCount;
        } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
            result = rowCount > 0;
        } else {
            throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
        }
        return result;
    }

    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
        if (void.class.equals(ms.getResultMaps().get(0).getType())) {
            throw new BindingException("method " + command.getName()
                    + " needs either a @ResultMap annotation, a @ResultType annotation,"
                    + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        }
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
        } else {
            sqlSession.select(command.getName(), param, method.extractResultHandler(args));
        }
    }

    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectList(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectList(command.getName(), param);
        }
        // issue #510 Collections & arrays support
        if (!method.getReturnType().isAssignableFrom(result.getClass())) {
            if (method.getReturnType().isArray()) {
                return convertToArray(result);
            } else {
                return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
            }
        }
        return result;
    }

    private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
        Cursor<T> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<T>selectCursor(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.<T>selectCursor(command.getName(), param);
        }
        return result;
    }

    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
        Object collection = config.getObjectFactory().create(method.getReturnType());
        MetaObject metaObject = config.newMetaObject(collection);
        metaObject.addAll(list);
        return collection;
    }

    @SuppressWarnings("unchecked")
    private <E> Object convertToArray(List<E> list) {
        Class<?> arrayComponentType = method.getReturnType().getComponentType();
        Object array = Array.newInstance(arrayComponentType, list.size());
        if (arrayComponentType.isPrimitive()) {
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        } else {
            return list.toArray((E[])array);
        }
    }

    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
        Map<K, V> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey(), rowBounds);
        } else {
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey());
        }
        return result;
    }

    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }

    /**
     * MapperMethod 的内部静态类，SQL 命令。
     * */
    @Getter
    public static class SqlCommand {

        /**
         * {@link MappedStatement#getId()}
         *
         *  实际上，就是 ${NAMESPACE_NAME}.${语句_ID}，
         *  例如："cn.lnd.ibatis.autoconstructor.AutoConstructorMapper.getSubject2"
         */
        private final String name;

        /**
         * SQL 命令类型
         *  cn.lnd.ibatis.mapping.SqlCommandType 类
         */
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            final String methodName = method.getName();
            final Class<?> declaringClass = method.getDeclaringClass(); // 声明方法的类
            // <1> 获得 MappedStatement 对象
            MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass, configuration);
            // <2> 找不到 MappedStatement
            if (ms == null) {
                // 如果有 @Flush 注解，则标记为 FLUSH 类型
                if(method.getAnnotation(Flush.class) != null){
                    name = null;
                    type = SqlCommandType.FLUSH;
                } else { // 抛出 BindingException 异常，如果找不到 MappedStatement
                    throw new BindingException("Invalid bound statement (not found): "
                            + mapperInterface.getName() + "." + methodName);
                }
            }
            // <3> 找到 MappedStatement
            else {
                // 获得 name 和 type
                name = ms.getId();
                type = ms.getSqlCommandType();
                if (type == SqlCommandType.UNKNOWN) { // 抛出 BindingException 异常，如果是 UNKNOWN 类型
                    throw new BindingException("Unknown execution method for: " + name);
                }
            }
        }

        private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                                                 Class<?> declaringClass, Configuration configuration) {
            // <1> 获得编号 【${NAMESPACE_NAME}.${语句_ID}】
            String statementId = mapperInterface.getName() + "." + methodName;
            // <2> 如果有，获得 MappedStatement 对象，并返回
            if (configuration.hasStatement(statementId)) {
                return configuration.getMappedStatement(statementId);
            }
            // 如果没有，并且当前方法就是 declaringClass 声明的，则说明真的找不到
            else if (mapperInterface.equals(declaringClass)) {
                return null;
            }
            // 遍历父接口，继续获得 MappedStatement 对象
            for (Class<?> superInterface : mapperInterface.getInterfaces()) {
                if (declaringClass.isAssignableFrom(superInterface)) {
                    MappedStatement ms = resolveMappedStatement(superInterface, methodName,
                            declaringClass, configuration);
                    if (ms != null) {
                        return ms;
                    }
                }
            }
            // 真的找不到，返回 null
            return null;
        }
    }

    /**
     * MapperMethod 的内部静态类，方法签名。
     * */
    public static class MethodSignature {

        /**
         * 返回类型是否为集合
         */
        private final boolean returnsMany;
        /**
         * 返回类型是否为 Map
         */
        private final boolean returnsMap;
        /**
         * 返回类型是否为 void
         */
        private final boolean returnsVoid;
        /**
         * 返回类型是否为 {@link cn.lnd.ibatis.cursor.Cursor}
         */
        private final boolean returnsCursor;
        /**
         * 返回类型是否为 {@link java.util.Optional}
         */
        private final boolean returnsOptional;
        /**
         * 返回类型
         */
        @Getter
        private final Class<?> returnType;
        /**
         * 返回方法上的 {@link MapKey#value()} ，前提是返回类型为 Map
         */
        @Getter
        private final String mapKey;
        /**
         * 获得 {@link ResultHandler} 在方法参数中的位置。
         *
         * 如果为 null ，说明不存在这个类型
         */
        private final Integer resultHandlerIndex;
        /**
         * 获得 {@link RowBounds} 在方法参数中的位置。
         *
         * 如果为 null ，说明不存在这个类型
         */
        private final Integer rowBoundsIndex;
        /**
         * ParamNameResolver 对象
         */
        private final ParamNameResolver paramNameResolver;

        public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
            // 初始化 returnType 属性
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
            if (resolvedReturnType instanceof Class<?>) { // 普通类
                this.returnType = (Class<?>) resolvedReturnType;
            } else if (resolvedReturnType instanceof ParameterizedType) { // 泛型
                this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
            } else { // 其它，内部类等等
                this.returnType = method.getReturnType();
            }
            // 初始化 returnsVoid 属性
            this.returnsVoid = void.class.equals(this.returnType);
            // 初始化 returnsMany 属性
            this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();
            // 初始化 returnsCursor 属性
            this.returnsCursor = Cursor.class.equals(this.returnType);
            // 初始化 returnsOptional 属性
            this.returnsOptional = Optional.class.equals(this.returnType);
            // <1> 初始化 mapKey
            this.mapKey = getMapKey(method);
            // 初始化 returnsMap
            this.returnsMap = this.mapKey != null;
            // <2> 初始化 rowBoundsIndex、resultHandlerIndex
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            // 初始化 paramNameResolver
            this.paramNameResolver = new ParamNameResolver(configuration, method);
        }

        /**
         * 获得 SQL 通用参数
         * */
        public Object convertArgsToSqlCommandParam(Object[] args) {
            return paramNameResolver.getNamedParams(args);
        }

        public boolean hasRowBounds() {
            return rowBoundsIndex != null;
        }

        public cn.lnd.ibatis.session.RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (cn.lnd.ibatis.session.RowBounds) args[rowBoundsIndex] : null;
        }

        public boolean hasResultHandler() {
            return resultHandlerIndex != null;
        }

        public cn.lnd.ibatis.session.ResultHandler extractResultHandler(Object[] args) {
            return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
        }

        public boolean returnsMany() {
            return returnsMany;
        }

        public boolean returnsMap() {
            return returnsMap;
        }

        public boolean returnsVoid() {
            return returnsVoid;
        }

        public boolean returnsCursor() {
            return returnsCursor;
        }

        /**
         * return whether return type is {@code java.util.Optional}
         * @return return {@code true}, if return type is {@code java.util.Optional}
         * @since 3.5.0
         */
        public boolean returnsOptional() {
            return returnsOptional;
        }

        /**
         * 获得指定参数类型在方法参数中的位置。
         * */
        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            // 遍历方法参数
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (paramType.isAssignableFrom(argTypes[i])) { // 类型符合
                    // 获得第一次的位置
                    if (index == null) {
                        index = i;
                        // 如果重复类型了，则抛出 BindingException 异常
                    } else {
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }

        /**
         * 获得注解的 {@link MapKey#value()}
         * */
        private String getMapKey(Method method) {
            String mapKey = null;
            // 返回类型为 Map
            if (Map.class.isAssignableFrom(method.getReturnType())) {
                // 使用 @MapKey 注解
                final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
                // 获得 @MapKey 注解的键
                if (mapKeyAnnotation != null) {
                    mapKey = mapKeyAnnotation.value();
                }
            }
            return mapKey;
        }
    }

}
