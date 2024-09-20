package cn.lnd.ibatis.scripting.xmltags;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 20:58
 */
public interface SqlNode {
    boolean apply(DynamicContext context);
}
