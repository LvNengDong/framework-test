package cn.lnd.ibatis.scripting.xmltags;

import cn.lnd.iba1tis.session.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:02
 */
public class SetSqlNode extends TrimSqlNode {

    private static List<String> suffixList = Arrays.asList(",");

    public SetSqlNode(Configuration configuration, SqlNode contents) {
        super(configuration, contents, "SET", null, null, suffixList);
    }

}
