package cn.lnd.tmp;

import lombok.Data;
import org.apache.ibatis.executor.SimpleExecutor;

import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/13 00:21
 */
@Data
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;
    // 处理器对象
    private Executor executor = new SimpleExecutor();

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(String statementId, Object... param) {
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        List<E> query = executor.query(configuration, mappedStatement, param);
        return query;
    }

    @Override
    public <T> T selectOne(String statementId, Object... param) {
        List<Object> objects = selectList(statementId, param);
        if (objects.size() == 1) {
            return (T) objects.get(0);
        } else {
            throw new RuntimeException("返回结果不止一个");
        }
    }

    @Override
    public void close() {
        executor.close();
    }
}
