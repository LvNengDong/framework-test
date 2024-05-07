package cn.lnd.refelct.base;

import cn.lnd.bean.Product;
import cn.lnd.bean.ProductAddition;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @Author lnd
 * @Description

PropertyDescriptor是Java中的一个类，它用于描述Java Bean中的属性。Java Bean是一种符合特定规范的Java类，其中包含私有属性、公共的getter和setter方法以及无参构造方法。

PropertyDescriptor类提供了一种方便的方式来访问和操作Java Bean中的属性。它可以通过反射机制获取属性的名称、类型以及对应的getter和setter方法。

使用PropertyDescriptor可以实现以下功能：

    获取属性的名称：通过getName()方法可以获取属性的名称。
    获取属性的类型：通过getPropertyType()方法可以获取属性的类型。
    获取属性的getter方法：通过getReadMethod()方法可以获取属性的getter方法。
    获取属性的setter方法：通过getWriteMethod()方法可以获取属性的setter方法。
    动态调用属性的getter和setter方法：通过getReadMethod().invoke(object)和getWriteMethod().invoke(object, value)可以动态调用属性的getter和setter方法。

 * @Date 2024/5/7 16:36
 */
public class PropertyDescriptorTest {

    public static void main(String[] args) throws Exception {
        // 创建一个Java Bean对象
        Product<ProductAddition> product = new Product<>();
        product.setProductName("标准大床房");


        // 获取属性的PropertyDescriptor对象
        PropertyDescriptor nameDescriptor = new PropertyDescriptor("productName", Product.class);

        // 获取属性的名称
        String propertyName = nameDescriptor.getName();
        System.out.println("Property Name: " + propertyName);

        // 获取属性的类型
        Class<?> propertyType = nameDescriptor.getPropertyType();
        System.out.println("Property Type: " + propertyType.getName());

        // 获取属性的getter方法
        Method getter = nameDescriptor.getReadMethod();
        System.out.println("Getter Method: " + getter.getName());

        // 获取属性的setter方法
        Method setter = nameDescriptor.getWriteMethod();
        System.out.println("Setter Method: " + setter.getName());

        // 动态调用属性的getter和setter方法
        Object nameValue = getter.invoke(product);
        System.out.println("Name Value: " + nameValue);

        setter.invoke(product, "标准双床房");
        System.out.println("Updated Name Value: " + product.getProductName());
    }
}

