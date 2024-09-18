package cn.lnd.java.reflect.type.demo02;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;
/**
 * @Author lnd
 * @Description
 * @Date 2024/9/6 21:10
 */
public class Main {
    public static void main(String[] args) {
        Type type = List[].class.getComponentType();
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type componentType = genericArrayType.getGenericComponentType();
            System.out.println("GenericArrayType component type: " + componentType);
        }
    }
}
