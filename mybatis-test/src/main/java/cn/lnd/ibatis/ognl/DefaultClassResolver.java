package cn.lnd.ibatis.ognl;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/27 12:11
 */
public class DefaultClassResolver implements ClassResolver {
    private final Map<String, Class> classes = new ConcurrentHashMap(101);

    public DefaultClassResolver() {
    }

    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Class result = (Class)this.classes.get(className);
        if (result != null) {
            return result;
        } else {
            result = className.indexOf(46) == -1 ? this.toClassForName("java.lang." + className) : this.toClassForName(className);
            this.classes.put(className, result);
            return result;
        }
    }

    protected Class toClassForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
