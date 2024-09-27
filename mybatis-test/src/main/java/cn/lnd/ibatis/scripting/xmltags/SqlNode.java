package cn.lnd.ibatis.scripting.xmltags;

/**
 * @Author lnd
 * @Description
 *
 *      MyBatis 会将 Mapper 映射文件中定义的 SQL 语句解析成 SqlSource 对象，其中的动态标签、SQL 语句文本等，会解析成对应类型的 SqlNode 对象。
 *
 * @Date 2024/9/20 20:58
 */
public interface SqlNode {
    boolean apply(DynamicContext context);
}
