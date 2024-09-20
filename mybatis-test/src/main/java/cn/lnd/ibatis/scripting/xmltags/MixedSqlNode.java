package cn.lnd.ibatis.scripting.xmltags;

import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:01
 */
public class MixedSqlNode implements cn.lnd.ibatis.scripting.xmltags.SqlNode {
    private List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents;

    public MixedSqlNode(List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents) {
        this.contents = contents;
    }

    @Override
    public boolean apply(DynamicContext context) {
        for (SqlNode sqlNode : contents) {
            sqlNode.apply(context);
        }
        return true;
    }
}
