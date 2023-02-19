package cn.lnd.tmp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/12 23:21
 */
@AllArgsConstructor
@Data
public class DefaultSqlSessionFactory implements SqlSessionFactory{

    private Configuration configuration;
    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }
}
