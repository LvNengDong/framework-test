import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author lnd
 * @Description
 * @Date 2023/5/10 11:43
 */
public class Test {

    public static void main(String[] args) {
        ArrayList<String> list = Lists.newArrayList();
        List<String> collect = list.stream()
                .map(e -> e + "xc")
                .collect(Collectors.toList());

        System.out.println(collect);
        System.out.println("=====");
    }
}
