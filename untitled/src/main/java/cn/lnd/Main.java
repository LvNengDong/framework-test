package cn.lnd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author lnd
 * @Description
 * @Date ${DATE} ${TIME}
 */
public class Main {
    //public static void main(String[] args) {
    //    //System.out.println("Hello world!");
    //    //boolean blank = StringUtils.isBlank(null);
    //    //System.out.println(blank);
    //    //testb();
    //
    //}
    public static void main(String[] args) {
        ArrayList<String> list = Lists.newArrayList("121", "2123", "23321");
        boolean b1 = list.contains("123");
        boolean b2 = list.contains("121");
        boolean b3 = list.contains("23321");
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);
    }
    //public static void test(){
    //    AtomicInteger beforeInt = new AtomicInteger(3);
    //    int intA = beforeInt.get();
    //    int intB = beforeInt.getAndSet(13);
    //    int intC = beforeInt.get();
    //    System.out.println(intA); // 3
    //    System.out.println(intB); // 3
    //    System.out.println(intC); // 13
    //}

    public static void test(){
        String s = "[{\"activiyNo\":\"20230627141347803443\",\"couponAmount\":\"3\",\"couponNo\":\"QUNAR024020231115036417\",\"couponUuid\":\"piao-030742b7-1d2a-4b8e-947b-d5a71df06d94\",\"limitRealName\":false,\"newflightCoupon\":true,\"strategyId\":\"90040155\"}]";
        JSONArray jsonArray = JSON.parseArray(s);

        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            String couponNo = jsonObject.getString("couponNo");
            System.out.printf(couponNo);
        }
        //JSONObject jsonObject = JSON.parseObject(jsonArray);
    }


    public static void test3(){
        long i = Long.parseLong("103590991393");
        System.out.println(i);
    }

    public static void testb(){
        while (true) {
            for (int i = 0; i < 10; i++) {
                if (i == 5) {
                    return; // 退出当前方法的执行
                }
                System.out.println(i);
            }
        }
        //System.out.println("This line will not be executed");
    }
}