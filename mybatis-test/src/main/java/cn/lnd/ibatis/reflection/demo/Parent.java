package cn.lnd.ibatis.reflection.demo;


import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/6 11:13
 */
public class Parent {
    public List<String> ids;

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }
}
