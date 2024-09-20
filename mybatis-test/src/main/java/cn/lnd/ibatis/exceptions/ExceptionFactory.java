package cn.lnd.ibatis.exceptions;

import cn.lnd.ibatis.executor.ErrorContext;

/**
 * @Author lnd
 * @Description 异常工厂
 * @Date 2024/9/18 23:49
 */
public class ExceptionFactory {

    private ExceptionFactory() {
        // Prevent Instantiation
    }

    /**
     * 包装异常成 PersistenceException
     *
     * @param message 消息
     * @param e 发生的异常
     * @return PersistenceException
     */
    public static RuntimeException wrapException(String message, Exception e) {
        return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
    }

}
