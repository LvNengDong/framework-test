package cn.lnd.ibatis.reflection.property;

import org.apache.ibatis.reflection.ReflectionException;

import java.util.Locale;

/**
 * @Author lnd
 * @Description 工具类；提供的功能是转换方法名到属性名，以及检测一个方法名是否为 getter 或 setter 方法。
 * @Date 2024/9/4 18:20
 */
public final class PropertyNamer {
    private PropertyNamer() {
    }

    /**
     * 根据方法名，获得对应的属性名
     * （不能解析 get/set/is 以外的方法）
     * @param name 方法名
     * @return
     */
    public static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else {
            // 抛出 ReflectionException 异常，因为只能处理 is、set、get 方法
            if (!name.startsWith("get") && !name.startsWith("set")) {
                throw new ReflectionException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
            }
            name = name.substring(3);
        }
        // 首字母小写
        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }

    /**
     * 判断是否为 is、get、set 方法
     * @param name 方法名
     * @return
     */
    public static boolean isProperty(String name) {
        return name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
    }

    /**
     * 判断是否为 get、is 方法
     * @param name 方法名
     * @return
     */
    public static boolean isGetter(String name) {
        return name.startsWith("get") || name.startsWith("is");
    }

    /**
     * 判断是否为 set 方法
     * @param name 方法名
     * @return
     */
    public static boolean isSetter(String name) {
        return name.startsWith("set");
    }
}
