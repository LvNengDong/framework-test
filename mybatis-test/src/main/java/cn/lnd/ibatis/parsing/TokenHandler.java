package cn.lnd.ibatis.parsing;

/**
 * @Author lnd
 * @Description Token 处理器接口
 * @Date 2024/9/19 17:27
 */
public interface TokenHandler {
    /**
     * 处理 Token
     *
     * @param content Token 字符串
     * @return 处理后的结果
     */
    String handleToken(String content);
}
