package cn.lnd.ibatis.ognl;

import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/27 12:11
 */
public interface ClassResolver {
    Class classForName(String var1, Map var2) throws ClassNotFoundException;
}
