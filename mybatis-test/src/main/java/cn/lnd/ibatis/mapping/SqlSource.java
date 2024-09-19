package cn.lnd.ibatis.mapping;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 14:47
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);

}
