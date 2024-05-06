package cn.lnd.time;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @Author lnd
 * @Description
 * @Date 2023/5/15 17:30
 */
public class DateUtil {

    // 计算两个时间点之间每天的周几
    public static void main(String[] args) {
        //String startDateString = "2023-05-11";
        //String endDateString = "2023-05-21";
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //LocalDate startDate = LocalDate.parse(startDateString, formatter);
        //LocalDate endDate = LocalDate.parse(endDateString, formatter);
        //
        //while (!startDate.isAfter(endDate)) {
        //    DayOfWeek dayOfWeek = startDate.getDayOfWeek();
        //    int value = dayOfWeek.getValue();
        //    String dayOfWeekString = dayOfWeek.toString();
        //    System.out.println(startDate + " is " + dayOfWeekString + "____" + value);
        //    startDate = startDate.plusDays(1);
        //}

        //
        //HashSet<Integer> set = Sets.newHashSet(1, 3, 4);
        //ArrayList<Integer> list = Lists.newArrayList(1, 4, 5, 6);
        //boolean b = list.containsAll(set);
        //System.out.println(b);


        //LocalDate startDate = LocalDate.of(2023, 5, 1);
        //LocalDate endDate = LocalDate.of(2023, 5, 31);
        //
        //LocalDate monday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        //
        //while (monday.isBefore(endDate)) {
        //    System.out.println("周一: " + monday);
        //    monday = monday.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        //}

        //LocalDate start = LocalDate.parse("2023-05-30");
        //LocalDate end = LocalDate.parse("2023-06-03");
        //Period period = Period.between(start, end);
        //System.out.println(period.getDays());

        BigDecimal a = new BigDecimal("10.00");
        BigDecimal b = new BigDecimal("5.00");

        int result = a.compareTo(b);

        if (result > 0) {
            System.out.println("a大于b");
        } else if (result < 0) {
            System.out.println("a小于b");
        } else {
            System.out.println("a等于b");
        }
    }


}
