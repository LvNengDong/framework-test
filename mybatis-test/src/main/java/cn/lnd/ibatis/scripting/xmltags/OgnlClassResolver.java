package cn.lnd.ibatis.scripting.xmltags;

import ognl.DefaultClassResolver;
import cn.lnd.ibatis.io.Resources;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 21:01
 */
public class OgnlClassResolver extends DefaultClassResolver {

    @Override
    protected Class toClassForName(String className) throws ClassNotFoundException {
        return Resources.classForName(className);
    }

}
