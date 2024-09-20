package cn.lnd.ibatis.scripting.xmltags;

import cn.lnd.ibatis.builder.BuilderException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:01
 */
public final class OgnlCache {

    private static final Map<String, Object> expressionCache = new ConcurrentHashMap<String, Object>();

    private OgnlCache() {
        // Prevent Instantiation of Static Class
    }

    public static Object getValue(String expression, Object root) {
        try {
            Map<Object, OgnlClassResolver> context = Ognl.createDefaultContext(root, new OgnlClassResolver());
            return Ognl.getValue(parseExpression(expression), context, root);
        } catch (OgnlException e) {
            throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    private static Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if (node == null) {
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }

}
