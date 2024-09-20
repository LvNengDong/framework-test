package cn.lnd.ibatis.scripting;

import cn.lnd.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:05
 */
public class ScriptingException extends PersistenceException {

    private static final long serialVersionUID = 7642570221267566591L;

    public ScriptingException() {
        super();
    }

    public ScriptingException(String message) {
        super(message);
    }

    public ScriptingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptingException(Throwable cause) {
        super(cause);
    }

}
