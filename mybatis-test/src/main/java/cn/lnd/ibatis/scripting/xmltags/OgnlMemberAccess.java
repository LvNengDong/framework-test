package cn.lnd.ibatis.scripting.xmltags;

import org.apache.ibatis.io.Resources;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/27 12:09
 */
public class OgnlClassResolver extends DefaultClassResolver {

    @Override
    protected Class toClassForName(String className) throws ClassNotFoundException {
        return Resources.classForName(className);
    }

}
