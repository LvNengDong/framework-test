import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @Author lnd
 * @Description
 * @Date 2023/3/9 16:00
 */

public class TestWork {

    @Test
    public void test01(){
        ArrayList<String> strings = Lists.newArrayList("qwe", "qwaa", "okds");
        boolean b = strings.contains("qwe");
        System.out.println(b);
    }

    private static final String INTER_PRE_SALE_ORDER_NO_PREFIX = "82";
    @Test
    public void test(){
        long parentOrderNo = 82103300441247L;
        String value = String.valueOf(parentOrderNo);
        if (StringUtils.startsWith(value, INTER_PRE_SALE_ORDER_NO_PREFIX)){
            System.out.println("国际预售单号忽略");
            return;
        }
        System.out.println("===========");
    }

}
