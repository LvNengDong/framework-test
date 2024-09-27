package cn.lnd.ibatis.mapping;

/**
 * @Author lnd
 * @Description
 *
 *      在内存中，MyBatis 使用 SqlSource 接口来表示 Mapper.xml 映射文件解析之后的 SQL 语句，其中的 SQL 语句只是一个中间态，可能包含动态 SQL 标签或占位符等信息，无法直接使用。
 * @Date 2024/9/19 14:47
 */
public interface SqlSource {

    // 根据Mapper文件或注解描述的SQL语句，以及传入的实参，返回可执行的SQL
    BoundSql getBoundSql(Object parameterObject);

}
