package cn.lnd.ibatis.logging.stdout;

import cn.lnd.ibatis.logging.Log;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 20:59
 */
public class StdOutImpl implements Log {

    public StdOutImpl(String clazz) {
        // Do Nothing
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void error(String s, Throwable e) {
        System.err.println(s);
        e.printStackTrace(System.err);
    }

    @Override
    public void error(String s) {
        System.err.println(s);
    }

    @Override
    public void debug(String s) {
        System.err.println(s);
    }

    @Override
    public void trace(String s) {
        System.err.println(s);
    }

    @Override
    public void warn(String s) {
        System.err.println(s);
    }
}
