package cn.lnd.ibatis.parsing;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 17:27
 */
public class ParsingException extends PersistenceException {
    private static final long serialVersionUID = -176685891441325943L;

    public ParsingException() {
        super();
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
