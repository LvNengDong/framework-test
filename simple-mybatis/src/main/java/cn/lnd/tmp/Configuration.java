package cn.lnd.tmp;

import lombok.Data;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/12 23:04
 */
@Data
public class Configuration {
    /**数据源*/
    private DataSource dataSource;

    /**map集合：key:statementId  value:MappedStatement*/
    private Map<String, MappedStatement> mappedStatementMap = new HashMap<>();


}
