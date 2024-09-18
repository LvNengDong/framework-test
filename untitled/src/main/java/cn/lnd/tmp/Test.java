package cn.lnd.tmp;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author lnd
 * @Description
 * @Date 2023/5/30 22:15
 */
public class Test {

    public static void main(String[] args) throws ParseException {
        //Set<String> strings = calculateDays("2023-06-03", "2023-06-04");
        //for (String string : strings) {
        //    System.out.println(string);
        //}
        //
        //LocalDate start = LocalDate.parse("2023-06-23");
        //System.out.println(start);
        //LocalDate end = LocalDate.parse("2023-06-22");
        //System.out.println(end);
        //// 如果start<end，这个方法计算出的是负数
        //Period period = Period.between(start, end);
        //int months = period.getMonths();
        //int days = period.getDays();
        //List<TemporalUnit> units = period.getUnits();
        //System.out.println(months + ":" + period.getDays());
        //
        //long daysBetween = ChronoUnit.DAYS.between(start, end);
        //System.out.println(daysBetween);

        //test();
        Test test = new Test();
        //test.test3();
        //test.test4();

        //test.test5();
        test.test6();
    }

    public void test6() {
        BigDecimal priceA = new BigDecimal(1);
        BigDecimal priceB = new BigDecimal(-1);
        System.out.println(priceA.subtract(priceB));
    }
    public void test5(){
        String from = "2023-06-05";
        String to = "2023-06-20";
        Set<String> strings = calculateDaysClosedInterval(from, to);
        List<String> sortedRes = strings.stream().sorted().collect(Collectors.toList());
        System.out.println(sortedRes);
    }
    public static Set<String> calculateDaysClosedInterval(String fromDate, String toDate) {
        Set<String> days = Sets.newHashSet();
        // 将字符串转换为LocalDate对象
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(fromDate, formatter);
        LocalDate endDate = LocalDate.parse(toDate, formatter).plusDays(-1);
        // 计算两个日期之间的天数，并遍历每一天
        long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= numOfDays; i++) {
            LocalDate day = startDate.plusDays(i);
            // 将每一天转换为字符串形式，并添加到列表中
            String dayStr = day.format(formatter);
            days.add(dayStr);
        }
        return days;
    }

    public void test4(){
        String date = "2023-06-16 00:00:00";
        String[] split = StringUtils.split(date, " ");
        System.out.println(split[0]);

    }

    public void test3() throws ParseException {
        String time = "2023-07-05";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //1.string->date
        Date parse = dateFormat.parse(time);
        int daysOfTwoDate = getDaysOfTwoDateCContainMinus(parse, new Date());
        System.out.println(daysOfTwoDate);
    }

    public static int getDaysOfTwoDateCContainMinus(Date startDate, Date endDate) {
        // 时间格式统一
        DateTime startDateTime = new DateTime(startDate);
        DateTime endDateTime = new DateTime(endDate);
        startDateTime = startDateTime.withTime(0, 0, 0, 0);
        endDateTime = endDateTime.withTime(0, 0, 0, 0);
        return Days.daysBetween(startDateTime, endDateTime).getDays();
    }

    private void test2() {
        Integer x = 0;
        Integer y = 0;
        add(x, y);
        System.out.println(x);
        System.out.println(y);
    }

    private void add(Integer x, Integer y) {
        for (int i = 1; i <= 10; i++) {
            x++;
            y--;
        }
        System.out.println("===x" + x);
        System.out.println("===y" + y);
    }

    public Set<String> calculateDays(String fromDate, String toDate) {
        Set<String> days = Sets.newHashSet();
        // 将字符串转换为LocalDate对象
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(fromDate, formatter);
        LocalDate endDate = LocalDate.parse(toDate, formatter).plusDays(-1);
        // 计算两个日期之间的天数，并遍历每一天
        long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= numOfDays; i++) {
            LocalDate day = startDate.plusDays(i);
            // 将每一天转换为字符串形式，并添加到列表中
            String dayStr = day.format(formatter);
            days.add(dayStr);
        }
        return days;
    }


    public static void test(){
        Date date = new Date();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());;
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        System.out.println(Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
    }
}
