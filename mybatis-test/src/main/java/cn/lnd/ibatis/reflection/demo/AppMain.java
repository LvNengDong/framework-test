package cn.lnd.ibatis.reflection.demo;

import cn.lnd.ibatis.reflection.Reflector;
import com.alibaba.fastjson.JSON;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/4 21:53
 */
public class AppMain {

    public static void main(String[] args) {
        //Reflector reflector = new Reflector(Product.class);
        Reflector reflector = new Reflector(Son.class);
        System.out.println(JSON.toJSONString(reflector));
    }

}
