package cn.lnd.ibatis.exceptions;


/**
 * @Author lnd
 * @Description 继承 IbatisException 类，目前 MyBatis 真正的异常基类
 * @Date 2024/9/18 21:54
 */
public class PersistenceException extends IbatisException {

    private static final long serialVersionUID = -7537395265357977271L;

    public PersistenceException() {
        super();
    }

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
