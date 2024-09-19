package cn.lnd.ibatis.session;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 21:13
 */
public class SqlSessionException extends PersistenceException {

    private static final long serialVersionUID = 3833184690240265047L;

    public SqlSessionException() {
        super();
    }

    public SqlSessionException(String message) {
        super(message);
    }

    public SqlSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlSessionException(Throwable cause) {
        super(cause);
    }
}
