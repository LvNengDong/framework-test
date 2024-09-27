package cn.lnd.ibatis.builder;

import cn.lnd.ibatis.mapping.ParameterMode;
import cn.lnd.ibatis.mapping.ResultSetType;
import cn.lnd.ibatis.session.Configuration;
import cn.lnd.ibatis.type.JdbcType;
import cn.lnd.ibatis.type.TypeAliasRegistry;
import cn.lnd.ibatis.type.TypeHandler;
import cn.lnd.ibatis.type.TypeHandlerRegistry;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Author lnd
 * @Description
 *      基础构造器抽象类，为子类提供通用的工具类。
 *
 *      MyBatis 框架中的一个基础构建器类。它是 MyBatis 构建器模式的基类，用于构建各种配置对象，如 Configuration、MapperBuilderAssistant 等。
 *      BaseBuilder 类提供了一些公共的方法和属性，用于处理配置文件中的通用元素，如类型别名、插件、属性等。它还提供了一些辅助方法，用于解析和处理配置文件中的各种元素，如解析 XML、创建对象实例等。
 * @Date 2024/9/18 23:38
 */
public abstract class BaseBuilder {

    /*
     * MyBatis Configuration 对象
     *      XML 和注解中解析到的配置，最终都会设置到 org.apache.ibatis.session.Configuration 中
     * */
    @Getter
    protected final Configuration configuration;

    /*
    * 别名注册中心
    * */
    protected final TypeAliasRegistry typeAliasRegistry;

    /*
    * TypeHandler 注册中心
    * */
    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    /**
     * 创建正则表达式
     *
     * @param regex 指定表达式
     * @param defaultValue 默认表达式
     * @return 正则表达式
     */
    protected Pattern parseExpression(String regex, String defaultValue) {
        return Pattern.compile(regex == null ? defaultValue : regex);
    }

    /*  #xxxValueOf(...) 方法，将字符串转换成对应的数据类型的值 */
    protected Boolean booleanValueOf(String value, Boolean defaultValue) {
        return value == null ? defaultValue : Boolean.valueOf(value);
    }
    protected Integer integerValueOf(String value, Integer defaultValue) {
        return value == null ? defaultValue : Integer.valueOf(value);
    }
    protected Set<String> stringSetValueOf(String value, String defaultValue) {
        value = (value == null ? defaultValue : value);
        return new HashSet<String>(Arrays.asList(value.split(",")));
    }

    /**
     * 根据给定的字符串类型，将其解析为对应的 JdbcType 类型。
     * @param alias 字符串类型
     * @return JdbcType 类型
     */
    protected JdbcType resolveJdbcType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return JdbcType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
        }
    }

    /**
     * 根据给定的字符串类型，将其解析为对应的 ResultSetType 类型。
     * @param alias 字符串类型
     * @return ResultSetType 类型
     */
    protected ResultSetType resolveResultSetType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return ResultSetType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving ResultSetType. Cause: " + e, e);
        }
    }

    /**
     * 根据给定的字符串类型，将其解析为对应的 ParameterMode 类型。
     * @param alias 字符串类型
     * @return ParameterMode 类型
     */
    protected ParameterMode resolveParameterMode(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return ParameterMode.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving ParameterMode. Cause: " + e, e);
        }
    }

    /**
     * 创建指定对象
     * @param alias 字符串类型
     * @return ParameterMode 类型
     */
    protected Object createInstance(String alias) {
        // <1> 获得对应的类型
        Class<?> clazz = resolveClass(alias);
        if (clazz == null) {
            return null;
        }
        try {
            // <2> 创建对象
            return resolveClass(alias).newInstance(); // 这里重复获得了一次
        } catch (Exception e) {
            throw new BuilderException("Error creating instance. Cause: " + e, e);
        }
    }

    /**
     * 通过别名或全限定类名，得到对应的 Class
     * @param alias 别名或全限定类名
     */
    protected <T> Class<? extends T> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new org.apache.ibatis.builder.BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    /**
     * 从 typeHandlerRegistry 中获得或创建对应的 TypeHandler 对象
     *
     * @param javaType
     * @param typeHandlerAlias
     * @return
     */
    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, String typeHandlerAlias) {
        if (typeHandlerAlias == null) {
            return null;
        }
        Class<?> type = resolveClass(typeHandlerAlias);
        if (type != null && !TypeHandler.class.isAssignableFrom(type)) {
            throw new BuilderException("Type " + type.getName() + " is not a valid TypeHandler because it does not implement TypeHandler interface");
        }
        @SuppressWarnings( "unchecked" ) // already verified it is a TypeHandler
        Class<? extends TypeHandler<?>> typeHandlerType = (Class<? extends TypeHandler<?>>) type;
        return resolveTypeHandler(javaType, typeHandlerType);
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null) {
            return null;
        }
        // javaType ignored for injected handlers see issue #746 for full detail
        // 先获得 TypeHandler 对象
        TypeHandler<?> handler = typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
        if (handler == null) {
            // not in registry, create a new one
            // 如果不存在，进行创建 TypeHandler 对象
            handler = typeHandlerRegistry.getInstance(javaType, typeHandlerType);
        }
        return handler;
    }

    /**
     * 从 typeAliasRegistry 中，通过别名或类全名，获得对应的类
     */
    protected <T> Class<? extends T> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }
}
