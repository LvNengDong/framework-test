package cn.lnd.ibatis.reflection;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/4 18:22
 */
public class ReflectionException extends PersistenceException {
    private static final long serialVersionUID = 7642570221267566591L;

    public ReflectionException() {
    }

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(Throwable cause) {
        super(cause);
    }
}
