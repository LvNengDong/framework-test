package cn.lnd.ibatis.type;

import cn.lnd.ibatis.io.ResolverUtil;
import cn.lnd.ibatis.io.Resources;
import cn.lnd.ibatis.type.*;
import cn.lnd.ibatis.type.JdbcType;
import cn.lnd.ibatis.type.TypeHandler;
import cn.lnd.ibatis.type.*;
import cn.lnd.ibatis.type.UnknownTypeHandler;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 11:34
 */
public class TypeHandlerRegistry {

    private final Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>>(cn.lnd.ibatis.type.JdbcType.class);
    private final Map<Type, Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>>> TYPE_HANDLER_MAP = new ConcurrentHashMap<Type, Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>>>();
    private final cn.lnd.ibatis.type.TypeHandler<Object> UNKNOWN_TYPE_HANDLER = new UnknownTypeHandler(this);
    private final Map<Class<?>, cn.lnd.ibatis.type.TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<Class<?>, cn.lnd.ibatis.type.TypeHandler<?>>();

    private static final Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> NULL_TYPE_HANDLER_MAP = new HashMap<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>>();

    public TypeHandlerRegistry() {
        register(Boolean.class, new BooleanTypeHandler());
        register(boolean.class, new BooleanTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.BOOLEAN, new BooleanTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.BIT, new BooleanTypeHandler());

        register(Byte.class, new ByteTypeHandler());
        register(byte.class, new ByteTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.TINYINT, new ByteTypeHandler());

        register(Short.class, new ShortTypeHandler());
        register(short.class, new ShortTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.SMALLINT, new ShortTypeHandler());

        register(Integer.class, new IntegerTypeHandler());
        register(int.class, new IntegerTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.INTEGER, new IntegerTypeHandler());

        register(Long.class, new LongTypeHandler());
        register(long.class, new LongTypeHandler());

        register(Float.class, new FloatTypeHandler());
        register(float.class, new FloatTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.FLOAT, new FloatTypeHandler());

        register(Double.class, new DoubleTypeHandler());
        register(double.class, new DoubleTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.DOUBLE, new DoubleTypeHandler());

        register(Reader.class, new ClobReaderTypeHandler());
        register(String.class, new StringTypeHandler());
        register(String.class, cn.lnd.ibatis.type.JdbcType.CHAR, new StringTypeHandler());
        register(String.class, cn.lnd.ibatis.type.JdbcType.CLOB, new ClobTypeHandler());
        register(String.class, cn.lnd.ibatis.type.JdbcType.VARCHAR, new StringTypeHandler());
        register(String.class, cn.lnd.ibatis.type.JdbcType.LONGVARCHAR, new ClobTypeHandler());
        register(String.class, cn.lnd.ibatis.type.JdbcType.NVARCHAR, new NStringTypeHandler());
        register(String.class, cn.lnd.ibatis.type.JdbcType.NCHAR, new NStringTypeHandler());
        register(String.class, cn.lnd.ibatis.type.JdbcType.NCLOB, new NClobTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.CHAR, new StringTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.VARCHAR, new StringTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.CLOB, new ClobTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.LONGVARCHAR, new ClobTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.NVARCHAR, new NStringTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.NCHAR, new NStringTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.NCLOB, new NClobTypeHandler());

        register(Object.class, cn.lnd.ibatis.type.JdbcType.ARRAY, new ArrayTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.ARRAY, new ArrayTypeHandler());

        register(BigInteger.class, new BigIntegerTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.BIGINT, new LongTypeHandler());

        register(BigDecimal.class, new BigDecimalTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.REAL, new BigDecimalTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.DECIMAL, new BigDecimalTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.NUMERIC, new BigDecimalTypeHandler());

        register(InputStream.class, new BlobInputStreamTypeHandler());
        register(Byte[].class, new ByteObjectArrayTypeHandler());
        register(Byte[].class, cn.lnd.ibatis.type.JdbcType.BLOB, new BlobByteObjectArrayTypeHandler());
        register(Byte[].class, cn.lnd.ibatis.type.JdbcType.LONGVARBINARY, new BlobByteObjectArrayTypeHandler());
        register(byte[].class, new ByteArrayTypeHandler());
        register(byte[].class, cn.lnd.ibatis.type.JdbcType.BLOB, new BlobTypeHandler());
        register(byte[].class, cn.lnd.ibatis.type.JdbcType.LONGVARBINARY, new BlobTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.LONGVARBINARY, new BlobTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.BLOB, new BlobTypeHandler());

        register(Object.class, UNKNOWN_TYPE_HANDLER);
        register(Object.class, cn.lnd.ibatis.type.JdbcType.OTHER, UNKNOWN_TYPE_HANDLER);
        register(cn.lnd.ibatis.type.JdbcType.OTHER, UNKNOWN_TYPE_HANDLER);

        register(Date.class, new DateTypeHandler());
        register(Date.class, cn.lnd.ibatis.type.JdbcType.DATE, new DateOnlyTypeHandler());
        register(Date.class, cn.lnd.ibatis.type.JdbcType.TIME, new TimeOnlyTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.TIMESTAMP, new DateTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.DATE, new DateOnlyTypeHandler());
        register(cn.lnd.ibatis.type.JdbcType.TIME, new TimeOnlyTypeHandler());

        register(java.sql.Date.class, new SqlDateTypeHandler());
        register(java.sql.Time.class, new SqlTimeTypeHandler());
        register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());

        // mybatis-typehandlers-jsr310
        try {
            // since 1.0.0
            register("java.time.Instant", "cn.lnd.ibatis.type.InstantTypeHandler");
            register("java.time.LocalDateTime", "cn.lnd.ibatis.type.LocalDateTimeTypeHandler");
            register("java.time.LocalDate", "cn.lnd.ibatis.type.LocalDateTypeHandler");
            register("java.time.LocalTime", "cn.lnd.ibatis.type.LocalTimeTypeHandler");
            register("java.time.OffsetDateTime", "cn.lnd.ibatis.type.OffsetDateTimeTypeHandler");
            register("java.time.OffsetTime", "cn.lnd.ibatis.type.OffsetTimeTypeHandler");
            register("java.time.ZonedDateTime", "cn.lnd.ibatis.type.ZonedDateTimeTypeHandler");
            // since 1.0.1
            register("java.time.Month", "cn.lnd.ibatis.type.MonthTypeHandler");
            register("java.time.Year", "cn.lnd.ibatis.type.YearTypeHandler");
            // since 1.0.2
            register("java.time.YearMonth", "cn.lnd.ibatis.type.YearMonthTypeHandler");
            register("java.time.chrono.JapaneseDate", "cn.lnd.ibatis.type.JapaneseDateTypeHandler");

        } catch (ClassNotFoundException e) {
            // no JSR-310 handlers
        }

        // issue #273
        register(Character.class, new CharacterTypeHandler());
        register(char.class, new CharacterTypeHandler());
    }

    public boolean hasTypeHandler(Class<?> javaType) {
        return hasTypeHandler(javaType, null);
    }

    public boolean hasTypeHandler(TypeReference<?> javaTypeReference) {
        return hasTypeHandler(javaTypeReference, null);
    }

    public boolean hasTypeHandler(Class<?> javaType, cn.lnd.ibatis.type.JdbcType jdbcType) {
        return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
    }

    public boolean hasTypeHandler(TypeReference<?> javaTypeReference, cn.lnd.ibatis.type.JdbcType jdbcType) {
        return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
    }

    public cn.lnd.ibatis.type.TypeHandler<?> getMappingTypeHandler(Class<? extends cn.lnd.ibatis.type.TypeHandler<?>> handlerType) {
        return ALL_TYPE_HANDLERS_MAP.get(handlerType);
    }

    public <T> cn.lnd.ibatis.type.TypeHandler<T> getTypeHandler(Class<T> type) {
        return getTypeHandler((Type) type, null);
    }

    public <T> cn.lnd.ibatis.type.TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
        return getTypeHandler(javaTypeReference, null);
    }

    public cn.lnd.ibatis.type.TypeHandler<?> getTypeHandler(cn.lnd.ibatis.type.JdbcType jdbcType) {
        return JDBC_TYPE_HANDLER_MAP.get(jdbcType);
    }

    public <T> cn.lnd.ibatis.type.TypeHandler<T> getTypeHandler(Class<T> type, cn.lnd.ibatis.type.JdbcType jdbcType) {
        return getTypeHandler((Type) type, jdbcType);
    }

    public <T> cn.lnd.ibatis.type.TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference, cn.lnd.ibatis.type.JdbcType jdbcType) {
        return getTypeHandler(javaTypeReference.getRawType(), jdbcType);
    }

    @SuppressWarnings("unchecked")
    private <T> cn.lnd.ibatis.type.TypeHandler<T> getTypeHandler(Type type, cn.lnd.ibatis.type.JdbcType jdbcType) {
        Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);
        cn.lnd.ibatis.type.TypeHandler<?> handler = null;
        if (jdbcHandlerMap != null) {
            handler = jdbcHandlerMap.get(jdbcType);
            if (handler == null) {
                handler = jdbcHandlerMap.get(null);
            }
            if (handler == null) {
                // #591
                handler = pickSoleHandler(jdbcHandlerMap);
            }
        }
        // type drives generics here
        return (cn.lnd.ibatis.type.TypeHandler<T>) handler;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> getJdbcHandlerMap(Type type) {
        Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);
        if (NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap)) {
            return null;
        }
        if (jdbcHandlerMap == null && type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            jdbcHandlerMap = getJdbcHandlerMapForSuperclass(clazz);
            if (jdbcHandlerMap != null) {
                TYPE_HANDLER_MAP.put(type, jdbcHandlerMap);
            } else if (clazz.isEnum()) {
                register(clazz, new EnumTypeHandler(clazz));
                return TYPE_HANDLER_MAP.get(clazz);
            }
        }
        if (jdbcHandlerMap == null) {
            TYPE_HANDLER_MAP.put(type, NULL_TYPE_HANDLER_MAP);
        }
        return jdbcHandlerMap;
    }

    private Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> getJdbcHandlerMapForSuperclass(Class<?> clazz) {
        Class<?> superclass =  clazz.getSuperclass();
        if (superclass == null || Object.class.equals(superclass)) {
            return null;
        }
        Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(superclass);
        if (jdbcHandlerMap != null) {
            return jdbcHandlerMap;
        } else {
            return getJdbcHandlerMapForSuperclass(superclass);
        }
    }

    private cn.lnd.ibatis.type.TypeHandler<?> pickSoleHandler(Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> jdbcHandlerMap) {
        cn.lnd.ibatis.type.TypeHandler<?> soleHandler = null;
        for (cn.lnd.ibatis.type.TypeHandler<?> handler : jdbcHandlerMap.values()) {
            if (soleHandler == null) {
                soleHandler = handler;
            } else if (!handler.getClass().equals(soleHandler.getClass())) {
                // More than one type handlers registered.
                return null;
            }
        }
        return soleHandler;
    }

    public cn.lnd.ibatis.type.TypeHandler<Object> getUnknownTypeHandler() {
        return UNKNOWN_TYPE_HANDLER;
    }

    public void register(cn.lnd.ibatis.type.JdbcType jdbcType, cn.lnd.ibatis.type.TypeHandler<?> handler) {
        JDBC_TYPE_HANDLER_MAP.put(jdbcType, handler);
    }

    //
    // REGISTER INSTANCE
    //

    // Only handler

    @SuppressWarnings("unchecked")
    public <T> void register(cn.lnd.ibatis.type.TypeHandler<T> typeHandler) {
        boolean mappedTypeFound = false;
        MappedTypes mappedTypes = typeHandler.getClass().getAnnotation(MappedTypes.class);
        if (mappedTypes != null) {
            for (Class<?> handledType : mappedTypes.value()) {
                register(handledType, typeHandler);
                mappedTypeFound = true;
            }
        }
        // @since 3.1.0 - try to auto-discover the mapped type
        if (!mappedTypeFound && typeHandler instanceof TypeReference) {
            try {
                TypeReference<T> typeReference = (TypeReference<T>) typeHandler;
                register(typeReference.getRawType(), typeHandler);
                mappedTypeFound = true;
            } catch (Throwable t) {
                // maybe users define the TypeReference with a different type and are not assignable, so just ignore it
            }
        }
        if (!mappedTypeFound) {
            register((Class<T>) null, typeHandler);
        }
    }

    // java type + handler

    public <T> void register(Class<T> javaType, cn.lnd.ibatis.type.TypeHandler<? extends T> typeHandler) {
        register((Type) javaType, typeHandler);
    }

    private <T> void register(Type javaType, cn.lnd.ibatis.type.TypeHandler<? extends T> typeHandler) {
        MappedJdbcTypes mappedJdbcTypes = typeHandler.getClass().getAnnotation(MappedJdbcTypes.class);
        if (mappedJdbcTypes != null) {
            for (cn.lnd.ibatis.type.JdbcType handledJdbcType : mappedJdbcTypes.value()) {
                register(javaType, handledJdbcType, typeHandler);
            }
            if (mappedJdbcTypes.includeNullJdbcType()) {
                register(javaType, null, typeHandler);
            }
        } else {
            register(javaType, null, typeHandler);
        }
    }

    public <T> void register(TypeReference<T> javaTypeReference, cn.lnd.ibatis.type.TypeHandler<? extends T> handler) {
        register(javaTypeReference.getRawType(), handler);
    }

    // java type + jdbc type + handler

    public <T> void register(Class<T> type, cn.lnd.ibatis.type.JdbcType jdbcType, cn.lnd.ibatis.type.TypeHandler<? extends T> handler) {
        register((Type) type, jdbcType, handler);
    }

    private void register(Type javaType, cn.lnd.ibatis.type.JdbcType jdbcType, cn.lnd.ibatis.type.TypeHandler<?> handler) {
        if (javaType != null) {
            Map<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>> map = TYPE_HANDLER_MAP.get(javaType);
            if (map == null) {
                map = new HashMap<cn.lnd.ibatis.type.JdbcType, cn.lnd.ibatis.type.TypeHandler<?>>();
                TYPE_HANDLER_MAP.put(javaType, map);
            }
            map.put(jdbcType, handler);
        }
        ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
    }

    //
    // REGISTER CLASS
    //

    // Only handler type

    public void register(Class<?> typeHandlerClass) {
        boolean mappedTypeFound = false;
        MappedTypes mappedTypes = typeHandlerClass.getAnnotation(MappedTypes.class);
        if (mappedTypes != null) {
            for (Class<?> javaTypeClass : mappedTypes.value()) {
                register(javaTypeClass, typeHandlerClass);
                mappedTypeFound = true;
            }
        }
        if (!mappedTypeFound) {
            register(getInstance(null, typeHandlerClass));
        }
    }

    // java type + handler type

    public void register(String javaTypeClassName, String typeHandlerClassName) throws ClassNotFoundException {
        register(Resources.classForName(javaTypeClassName), Resources.classForName(typeHandlerClassName));
    }

    public void register(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
        register(javaTypeClass, getInstance(javaTypeClass, typeHandlerClass));
    }

    // java type + jdbc type + handler type

    public void register(Class<?> javaTypeClass, JdbcType jdbcType, Class<?> typeHandlerClass) {
        register(javaTypeClass, jdbcType, getInstance(javaTypeClass, typeHandlerClass));
    }

    // Construct a handler (used also from Builders)

    @SuppressWarnings("unchecked")
    public <T> cn.lnd.ibatis.type.TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
        if (javaTypeClass != null) {
            try {
                Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
                return (cn.lnd.ibatis.type.TypeHandler<T>) c.newInstance(javaTypeClass);
            } catch (NoSuchMethodException ignored) {
                // ignored
            } catch (Exception e) {
                throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
            }
        }
        try {
            Constructor<?> c = typeHandlerClass.getConstructor();
            return (cn.lnd.ibatis.type.TypeHandler<T>) c.newInstance();
        } catch (Exception e) {
            throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
        }
    }

    // scan

    public void register(String packageName) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
        resolverUtil.find(new ResolverUtil.IsA(cn.lnd.ibatis.type.TypeHandler.class), packageName);
        Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
        for (Class<?> type : handlerSet) {
            //Ignore inner classes and interfaces (including package-info.java) and abstract classes
            if (!type.isAnonymousClass() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
                register(type);
            }
        }
    }

    // get information

    /**
     * @since 3.2.2
     */
    public Collection<TypeHandler<?>> getTypeHandlers() {
        return Collections.unmodifiableCollection(ALL_TYPE_HANDLERS_MAP.values());
    }

}
