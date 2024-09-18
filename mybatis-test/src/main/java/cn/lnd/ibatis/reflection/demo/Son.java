package cn.lnd.ibatis.reflection.demo;


import java.util.ArrayList;


/**
 * @Author lnd
 * @Description
 * @Date 2024/9/6 11:14
 */
public class Son extends Parent {
    @Override
    public ArrayList<String> getIds() {
        return new ArrayList<>();
    }
}
