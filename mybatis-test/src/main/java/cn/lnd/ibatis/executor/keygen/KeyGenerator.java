package cn.lnd.ibatis.executor.keygen;

import cn.lnd.ibatis.executor.Executor;
import cn.lnd.ibatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 14:42
 */
public interface KeyGenerator {

    void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

    void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
