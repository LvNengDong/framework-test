package cn.lnd.qunar;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/10 21:00
 */
public class TestMapPutall {
    public static void main(String[] args) {
        Map<String, String> extraMap = new HashMap<>();
        extraMap.put("1", "aaa");
        extraMap.put("2", "bbb");
        Map<String, String> trackData = new HashMap<>();
        trackData.put("a", "111");
        trackData.put("b", "222");
        extraMap.putAll(trackData);

        System.out.printf("res" + extraMap);
    }
}
