package cn.lnd.test.ognl;


import cn.lnd.test.model.Address;
import cn.lnd.test.model.Customer;
import cn.lnd.ibatis.scripting.xmltags.OgnlMemberAccess;
import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlContext;

import java.util.ArrayList;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/27 20:53
 */
public class OGNLDemo {

    private static Customer customer;
    private static OgnlContext context;

    private static Customer createCustomer() {
        customer = new Customer();
        customer.setId(1);
        customer.setName("Test Customer");
        customer.setPhone("1234567");
        Address address = new Address();
        address.setCity("city-001");
        address.setId(1);
        address.setCountry("country-001");
        address.setStreet("street-001");
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(address);
        customer.setAddresses(addresses);
        return customer;
    }

    public static void main(String[] args) throws Exception {
        customer = createCustomer(); // 创建Customer对象以及Address对象
        // 创建OgnlContext上下文对象
        context = new OgnlContext(new DefaultClassResolver(), new DefaultTypeConverter(), new OgnlMemberAccess());
        // 设置root以及address这个key，默认从root开始查找属性或方法
        context.setRoot(customer);
        context.put("address", customer.getAddresses().get(0));
        // Ognl.paraseExpression()方法负责解析OGNL表达式，获取Customer的addresses属性
        Object obj = Ognl.getValue(Ognl.parseExpression("addresses"), context, context.getRoot());
        System.out.println(obj);
        // 输出是[Address{id=1, street='street-001', city='city-001', country='country-001'}]
        // 获取city属性
        obj = Ognl.getValue(Ognl.parseExpression("addresses[0].city"), context, context.getRoot());
        System.out.println(obj); // 输出是city-001
        // #address表示访问的不是root对象，而是OgnlContext中key为addresses的对象
        obj = Ognl.getValue(Ognl.parseExpression("#address.city"), context, context.getRoot());
        System.out.println(obj); // 输出是city-001
        // 执行Customer的getName()方法
        obj = Ognl.getValue(Ognl.parseExpression("getName()"), context, context.getRoot());
        System.out.println(obj);
        // 输出是Test Customer
    }
}
