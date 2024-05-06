package cn.lnd.jdk8;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2023/5/21 22:23
 */
public class TestClose {
    public static void main(String[] args) {
        ArrayList<Integer> list = Lists.newArrayList(1, 2, 3);
        ArrayList<Integer> ji = Lists.newArrayList();
        ArrayList<Integer> ou = Lists.newArrayList();
        //
        //list.stream()
        //        .(e -> {
        //            if (e%2==0) {
        //                ou.add(e);
        //            } else {
        //                ji.add(e);
        //            }
        //        })
        //        .count();

        System.out.println(ji);
        System.out.println(ou);
    }
}
