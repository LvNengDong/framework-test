package cn.lnd.tmp;

import java.math.BigDecimal;

/**
 * @Author lnd
 * @Description
 * @Date 2024/2/1 14:51
 */
public class Test2 {
    public static void main(String[] args) {
        boolean b = BigDecimal.ZERO.compareTo(new BigDecimal(2.00)) < 0;
        System.out.println(b);
    }
}
