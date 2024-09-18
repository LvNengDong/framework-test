package cn.lnd.ibatis.datasource;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/16 22:57
 */
public class DataSourceException extends PersistenceException {

    private static final long serialVersionUID = -5251396250407091334L;

    public DataSourceException() {
        super();
    }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }

}
