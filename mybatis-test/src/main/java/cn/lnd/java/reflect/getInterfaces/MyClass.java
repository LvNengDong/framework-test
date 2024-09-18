package cn.lnd.java.reflect.getInterfaces;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/5 17:41
 */
public class MyClass implements MyInterface1, MyInterface2 {
    /*
    java.lang.Class#getInterfaces() 方法用于获取当前类所实现的接口数组。

    在Java中，一个类可以实现一个或多个接口。通过实现接口，类可以获得接口中定义的方法和常量，并且必须实现接口中声明的所有方法。
    getInterfaces() 方法返回一个包含当前类所实现的接口的 Class 对象数组。
    该方法的返回值是一个 Class 对象数组，每个元素表示一个接口。通过遍历这个数组，可以获取到当前类所实现的所有接口。
    */
    public static void main(String[] args) {
        Class<?>[] interfaces = MyClass.class.getInterfaces();
        for (Class<?> intf : interfaces) {
            System.out.println(intf.getName());
        }
    }
}
