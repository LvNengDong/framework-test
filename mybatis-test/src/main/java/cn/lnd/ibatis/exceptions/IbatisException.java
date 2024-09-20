package cn.lnd.ibatis.exceptions;

/**
 * @Author lnd
 * @Description
 *      IBatis 的异常基类
 *      实际上，IbatisException 已经在 2015 年被废弃，取代它的是 PersistenceException 类。
 * @Date 2024/9/18 21:54
 */
@Deprecated
public class IbatisException extends RuntimeException {

    private static final long serialVersionUID = 3880206998166270511L;

    public IbatisException() {
        super();
    }

    public IbatisException(String message) {
        super(message);
    }

    public IbatisException(String message, Throwable cause) {
        super(message, cause);
    }

    public IbatisException(Throwable cause) {
        super(cause);
    }

}
