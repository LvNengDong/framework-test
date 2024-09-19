package cn.lnd.ibatis.exceptions;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 17:36
 */
public class TooManyResultsException extends PersistenceException {

    private static final long serialVersionUID = 8935197089745865786L;

    public TooManyResultsException() {
        super();
    }

    public TooManyResultsException(String message) {
        super(message);
    }

    public TooManyResultsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManyResultsException(Throwable cause) {
        super(cause);
    }
}
