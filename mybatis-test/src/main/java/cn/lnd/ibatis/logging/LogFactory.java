package cn.lnd.ibatis.logging;

import cn.lnd.ibatis.logging.slf4j.Slf4jImpl;
import cn.lnd.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.logging.LogException;

import java.lang.reflect.Constructor;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 20:28
 */
public class LogFactory {
    /**
     * Marker to be used by logging implementations that support markers
     */
    public static final String MARKER = "MYBATIS";

    /**
     * 使用的 Log 的构造方法
     *
     * 记录当前使用的第三方日志库适配器的构造方法
     */
    private static Constructor<? extends Log> logConstructor;

    static {
        // <1> 逐个尝试，判断使用哪个 Log 的实现类，即初始化 logConstructor 属性
        tryImplementation(LogFactory::useSlf4jLogging);
        tryImplementation(LogFactory::useCommonsLogging);
        tryImplementation(LogFactory::useLog4J2Logging);
        tryImplementation(LogFactory::useLog4JLogging);
        tryImplementation(LogFactory::useJdkLogging);
        tryImplementation(LogFactory::useNoLogging);
    }

    private LogFactory() {
        // disable construction
    }

    private static void tryImplementation(Runnable runnable) {
        if (logConstructor == null) {
            try {
                runnable.run();
            } catch (Throwable t) {
                // ignore
            }
        }
    }


    /**
     * 尝试使用 Slf4j
     */
    public static synchronized void useSlf4jLogging() {
        setImplementation(Slf4jImpl.class);
    }

    public static synchronized void useCommonsLogging() {
        //setImplementation(org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl.class);
    }

    public static synchronized void useLog4JLogging() {
        //setImplementation(org.apache.ibatis.logging.log4j.Log4jImpl.class);
    }

    public static synchronized void useLog4J2Logging() {
        //setImplementation(org.apache.ibatis.logging.log4j2.Log4j2Impl.class);
    }

    public static synchronized void useJdkLogging() {
        //setImplementation(org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl.class);
    }

    public static synchronized void useStdOutLogging() {
        setImplementation(StdOutImpl.class);
    }

    public static synchronized void useNoLogging() {
        //setImplementation(org.apache.ibatis.logging.nologging.NoLoggingImpl.class);
    }

    /**
     * 设置自定义的 Log 实现类。
     *  这里的自定义，可以是你自己实现的 Log 类，也可以是上述的 MyBatis 内置的 Log 实现类。
     * @param clazz
     */
    public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
        setImplementation(clazz);
    }

    /**
     * 尝试使用指定的 Log 实现类
     *
     * @param implClass
     */
    private static void setImplementation(Class<? extends Log> implClass) {
        try {
            // 获得参数为 String 的构造方法
            Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
            // 创建 Log 对象
            Log log = candidate.newInstance(LogFactory.class.getName());
            if (log.isDebugEnabled()) {
                log.debug("Logging initialized using '" + implClass + "' adapter.");
            }
            // 创建成功，意味着可以使用，设置为 logConstructor
            logConstructor = candidate;
        } catch (Throwable t) {
            throw new LogException("Error setting Log implementation.  Cause: " + t, t);
        }
    }

    /**
     * 获得 Log 对象
     *
     * @param aClass
     * @return
     */
    public static Log getLog(Class<?> aClass) {
        return getLog(aClass.getName());
    }

    public static Log getLog(String logger) {
        try {
            return logConstructor.newInstance(logger);
        } catch (Throwable t) {
            throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
        }
    }


}

/*

    <1> 处，我们可以看到，按照 Slf4j、CommonsLogging、Log4J2Logging、Log4JLogging、JdkLogging、NoLogging 的顺序，逐个尝试，
    判断使用哪个 Log 的实现类，即初始化 logConstructor 属性。

 */