package cn.lnd.ibatis.logging;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 21:29
 */
public class LogException extends PersistenceException {

    private static final long serialVersionUID = 1022924004852350942L;

    public LogException() {
        super();
    }

    public LogException(String message) {
        super(message);
    }

    public LogException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogException(Throwable cause) {
        super(cause);
    }

}
