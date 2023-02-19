package cn.lnd.tmp;

import java.sql.SQLException;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/13 00:29
 */
public interface Executor {

    <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object[] param) throws SQLException, Exception;

    void close();
}
