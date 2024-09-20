package cn.lnd.ibatis.scripting.xmltags;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:02
 */
public class StaticTextSqlNode implements SqlNode {
    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public boolean apply(DynamicContext context) {
        context.appendSql(text);
        return true;
    }

}
