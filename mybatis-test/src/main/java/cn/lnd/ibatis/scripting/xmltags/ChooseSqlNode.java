package cn.lnd.ibatis.scripting.xmltags;

import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 20:58
 */
public class ChooseSqlNode implements cn.lnd.ibatis.scripting.xmltags.SqlNode {
    private cn.lnd.ibatis.scripting.xmltags.SqlNode defaultSqlNode;
    private List<cn.lnd.ibatis.scripting.xmltags.SqlNode> ifSqlNodes;

    public ChooseSqlNode(List<cn.lnd.ibatis.scripting.xmltags.SqlNode> ifSqlNodes, cn.lnd.ibatis.scripting.xmltags.SqlNode defaultSqlNode) {
        this.ifSqlNodes = ifSqlNodes;
        this.defaultSqlNode = defaultSqlNode;
    }

    @Override
    public boolean apply(DynamicContext context) {
        for (SqlNode sqlNode : ifSqlNodes) {
            if (sqlNode.apply(context)) {
                return true;
            }
        }
        if (defaultSqlNode != null) {
            defaultSqlNode.apply(context);
            return true;
        }
        return false;
    }
}
