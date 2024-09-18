package cn.lnd.java.reflect.type.demo01;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/6 21:05
 */
public class Main {
    public static void main(String[] args) {
        TypeVariable<Class<List>> typeVariable = List.class.getTypeParameters()[0];
        String name = typeVariable.getName();
        Type[] bounds = typeVariable.getBounds();
        System.out.println("TypeVariable name: " + name);
        System.out.println("TypeVariable bounds: " + bounds[0]);
    }
}
