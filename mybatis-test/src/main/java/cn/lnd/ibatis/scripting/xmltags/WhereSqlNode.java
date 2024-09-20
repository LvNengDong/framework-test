package cn.lnd.ibatis.scripting.xmltags;

import cn.lnd.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:03
 */
public class WhereSqlNode extends TrimSqlNode {

    private static List<String> prefixList = Arrays.asList("AND ","OR ","AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");

    public WhereSqlNode(Configuration configuration, SqlNode contents) {
        super(configuration, contents, "WHERE", prefixList, null, null);
    }

}
