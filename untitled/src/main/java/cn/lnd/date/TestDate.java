package cn.lnd.date;

import java.time.LocalDate;
import java.time.Period;

/**
 * @Author lnd
 * @Description
 * @Date 2023/5/21 18:38
 */
public class TestDate {
    public static void main(String[] args) {
        String fromDate = "2023-05-19";
        String toDate = "2023-05-21";
        LocalDate start = LocalDate.parse(toDate);
        LocalDate end = LocalDate.parse(fromDate);
        Period period = Period.between(start, end);
        System.out.println(period.getDays());
    }
}
