package cn.lnd.date;

import java.util.Calendar;
import java.util.Date;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/8 16:21
 */
public class CalendarTest {
    public static void main(String[] args) {
        new CalendarTest().test1();
    }

    private void test1() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        System.out.println(calendar.getTime());
        // T+1~T+2
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        System.out.println(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, 2);
        System.out.println(calendar.getTime());
    }
}
