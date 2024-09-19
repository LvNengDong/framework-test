package cn.lnd.ibatis.executor;

import cn.lnd.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 14:43
 */
public class ExecutorException extends PersistenceException {

    private static final long serialVersionUID = 4060977051977364820L;

    public ExecutorException() {
        super();
    }

    public ExecutorException(String message) {
        super(message);
    }

    public ExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutorException(Throwable cause) {
        super(cause);
    }

}
