package cn.lnd.ibatis.scripting.xmltags;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:00
 */
public class IfSqlNode implements SqlNode {
    private cn.lnd.ibatis.scripting.xmltags.ExpressionEvaluator evaluator;
    private String test;
    private cn.lnd.ibatis.scripting.xmltags.SqlNode contents;

    public IfSqlNode(cn.lnd.ibatis.scripting.xmltags.SqlNode contents, String test) {
        this.test = test;
        this.contents = contents;
        this.evaluator = new ExpressionEvaluator();
    }

    @Override
    public boolean apply(DynamicContext context) {
        if (evaluator.evaluateBoolean(test, context.getBindings())) {
            contents.apply(context);
            return true;
        }
        return false;
    }

}
