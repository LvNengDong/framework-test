package cn.lnd.apache.commons.collection;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/19 13:09
 */

public class TestCollects {
    @Test
    public void testIsEmpty(){
        ArrayList<Object> list = Lists.newArrayList();
        boolean b = CollectionUtils.isEmpty(list);
        System.out.println("Collection size is zero, result: " + b);
        list.add("hello");
        boolean b2 = CollectionUtils.isEmpty(list);
        System.out.println("Collection size is not zero, result: " + b2);

        /* 执行以上代码，输出结果为：
        --------------------------------------
        Collection size is zero, result: true
        Collection size is not zero, result: false
        -----------------------------------
        分析：CollectionUtils 会在集合中元素数量为0时也判断其为empty
        */
    }

}

