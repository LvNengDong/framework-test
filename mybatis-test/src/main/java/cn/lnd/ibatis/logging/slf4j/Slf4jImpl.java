package cn.lnd.ibatis.logging.slf4j;

import cn.lnd.ibatis.logging.Log;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 21:02
 */
public class Slf4jImpl implements Log {

    private Log log;

    public Slf4jImpl(String clazz) {
        // 使用 SLF_LoggerFactory 获得 SLF Logger 对象
        Logger logger = LoggerFactory.getLogger(clazz);

        // 如果是 LocationAwareLogger ，则创建 Slf4jLocationAwareLoggerImpl 对象
        if (logger instanceof LocationAwareLogger) {
            try {
                // check for slf4j >= 1.6 method signature
                logger.getClass().getMethod("log", Marker.class, String.class, int.class, String.class, Object[].class, Throwable.class);
                log = new Slf4jLocationAwareLoggerImpl((LocationAwareLogger) logger);
                return;
            } catch (SecurityException | NoSuchMethodException e) {
                // fail-back to Slf4jLoggerImpl
            }
        }

        // Logger is not LocationAwareLogger or slf4j version < 1.6
        // 否则，创建 Slf4jLoggerImpl 对象
        log = new Slf4jLoggerImpl(logger);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public void error(String s, Throwable e) {
        log.error(s, e);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void debug(String s) {
        log.debug(s);
    }

    @Override
    public void trace(String s) {
        log.trace(s);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }
}

/*
    在构造方法中，可以看到，适配不同的 SLF4J 的版本，分别使用 org.apache.ibatis.logging.slf4j.Slf4jLocationAwareLoggerImpl 和 org.apache.ibatis.logging.slf4j.Slf4jLoggerImpl 类。
    具体的方法实现，委托调用对应的 SLF4J 的方法。

*/
