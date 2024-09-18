package cn.lnd.java.reflect.type.demo03;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/6 21:12
 */
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Main {
    public static void main(String[] args) {
        ParameterizedType parameterizedType = (ParameterizedType) MyClass.class.getGenericSuperclass();
        Type rawType = parameterizedType.getRawType();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        System.out.println("ParameterizedType raw type: " + rawType);
        System.out.println("ParameterizedType actual type arguments: " + actualTypeArguments[0]);
    }
}

class MyClass<T> extends MySuperClass<String> {
    // 类定义
}

class MySuperClass<U> {
    // 类定义
}
