package cn.lnd.ibatis.exceptions;

import cn.lnd.ibatis.executor.ErrorContext;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/18 23:49
 */
public class ExceptionFactory {

    private ExceptionFactory() {
        // Prevent Instantiation
    }

    public static RuntimeException wrapException(String message, Exception e) {
        return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
    }

}
