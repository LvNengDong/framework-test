package cn.lnd.ibatis.reflection;


import cn.lnd.ibatis.reflection.invoker.GetFieldInvoker;
import cn.lnd.ibatis.reflection.invoker.Invoker;
import cn.lnd.ibatis.reflection.invoker.MethodInvoker;
import cn.lnd.ibatis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @Author lnd
 * @Description
 *      类的元数据，基于 Reflector 和 PropertyTokenizer ，提供对指定类的各种骚操作。
 * @Date 2024/9/11 16:21
 */
public class MetaClass {

    private ReflectorFactory reflectorFactory;
    private Reflector reflector;

    /**
     * 通过构造方法，我们可以看出，一个 MetaClass 对象，对应一个 Class 对象。
     * 私有方法，不对外暴露
     *
     * @param type
     * @param reflectorFactory
     */
    private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
        this.reflector = reflectorFactory.findForClass(type);
    }

    /**
     * 创建指定类的 MetaClass 对象
     *
     * @param type
     * @param reflectorFactory
     * @return
     */
    public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
        return new MetaClass(type, reflectorFactory);
    }

    /**
     * 创建类的指定属性的类的 MetaClass 对象
     *
     * @param name
     * @return
     */
    public MetaClass metaClassForProperty(String name) {
        // 获得属性的类
        Class<?> propType = reflector.getGetterType(name);
        // 创建 MetaClass 对象
        return MetaClass.forClass(propType, reflectorFactory);
    }

    /**
     * 根据表达式，获得属性
     */
    public String findProperty(String name) {
        // <3> 构建属性
        StringBuilder prop = buildProperty(name, new StringBuilder());
        return prop.length() > 0 ? prop.toString() : null;
    }


    public String findProperty(String name, boolean useCamelCaseMapping) {
        // <1> 下划线转驼峰（在 <1> 处，我们仅仅看到 _ 被替换成了空串。  解决“下划线转驼峰”的关键是，通过 `Reflector.caseInsensitivePropertyMap` 属性，忽略大小写。）
        if (useCamelCaseMapping) {
            name = name.replace("_", "");
        }
        // <2> 获得属性
        return findProperty(name);
    }

    public String[] getGetterNames() {
        return reflector.getGetablePropertyNames();
    }

    public String[] getSetterNames() {
        return reflector.getSetablePropertyNames();
    }

    public Class<?> getSetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop.getName());
            return metaProp.getSetterType(prop.getChildren());
        } else {
            return reflector.getSetterType(prop.getName());
        }
    }

    public Class<?> getGetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop);
            return metaProp.getGetterType(prop.getChildren());
        }
        // issue #506. Resolve the type inside a Collection Object
        return getGetterType(prop);
    }

    private MetaClass metaClassForProperty(PropertyTokenizer prop) {
        Class<?> propType = getGetterType(prop);
        return MetaClass.forClass(propType, reflectorFactory);
    }

    private Class<?> getGetterType(PropertyTokenizer prop) {
        Class<?> type = reflector.getGetterType(prop.getName());
        if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
            Type returnType = getGenericGetterType(prop.getName());
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnType = actualTypeArguments[0];
                    if (returnType instanceof Class) {
                        type = (Class<?>) returnType;
                    } else if (returnType instanceof ParameterizedType) {
                        type = (Class<?>) ((ParameterizedType) returnType).getRawType();
                    }
                }
            }
        }
        return type;
    }

    private Type getGenericGetterType(String propertyName) {
        try {
            Invoker invoker = reflector.getGetInvoker(propertyName);
            if (invoker instanceof MethodInvoker) {
                Field _method = MethodInvoker.class.getDeclaredField("method");
                _method.setAccessible(true);
                Method method = (Method) _method.get(invoker);
                return TypeParameterResolver.resolveReturnType(method, reflector.getType());
            } else if (invoker instanceof GetFieldInvoker) {
                Field _field = GetFieldInvoker.class.getDeclaredField("field");
                _field.setAccessible(true);
                Field field = (Field) _field.get(invoker);
                return TypeParameterResolver.resolveFieldType(field, reflector.getType());
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public boolean hasSetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasSetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop.getName());
                return metaProp.hasSetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasSetter(prop.getName());
        }
    }

    /**
     * 判断指定属性是否有 getting 方法
     *
     * @param name
     * @return
     */
    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasGetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop);
                return metaProp.hasGetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasGetter(prop.getName());
        }
    }

    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }

    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }

    private StringBuilder buildProperty(String name, StringBuilder builder) {
        // 创建 PropertyTokenizer 对象，对 name 进行分词
        PropertyTokenizer prop = new PropertyTokenizer(name);
        // 有子表达式
        if (prop.hasNext()) {
            // <4> 获得属性名，并添加到 builder 中
            String propertyName = reflector.findPropertyName(prop.getName());
            if (propertyName != null) {
                builder.append(propertyName);
                builder.append(".");
                // 创建 MetaClass 对象
                MetaClass metaProp = metaClassForProperty(propertyName);
                // 递归解析子表达式 children ，并将结果添加到 builder 中
                metaProp.buildProperty(prop.getChildren(), builder);
            }
        }
        // 无子表达式
        else {
            // <4> 获得属性名，并添加到 builder 中
            String propertyName = reflector.findPropertyName(name);
            if (propertyName != null) {
                builder.append(propertyName);
            }
        }
        return builder;
    }

    public boolean hasDefaultConstructor() {
        return reflector.hasDefaultConstructor();
    }

}
