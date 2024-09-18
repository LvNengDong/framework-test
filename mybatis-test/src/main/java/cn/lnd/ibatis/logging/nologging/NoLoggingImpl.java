package cn.lnd.ibatis.logging.nologging;


import cn.lnd.ibatis.logging.Log;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 21:51
 */
public class NoLoggingImpl implements Log {

    public NoLoggingImpl(String clazz) {
        // Do Nothing
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void error(String s, Throwable e) {
        // Do Nothing
    }

    @Override
    public void error(String s) {
        // Do Nothing
    }

    @Override
    public void debug(String s) {
        // Do Nothing
    }

    @Override
    public void trace(String s) {
        // Do Nothing
    }

    @Override
    public void warn(String s) {
        // Do Nothing
    }
}

