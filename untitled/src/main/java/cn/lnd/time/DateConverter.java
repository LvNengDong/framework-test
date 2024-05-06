package cn.lnd.time;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author lnd
 * @Description
 * @Date 2023/9/13 16:08
 */
public class DateConverter {

    public static Date convertStringToDate(String dateString, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void test(){
        String a = "吕能东";
        String b = "肖明亮";
        String content = "xxxxx";
        List<String> noticeUser = Lists.newArrayList();
        noticeUser.add(a);
        noticeUser.add(b);


        StringBuffer stringBuffer = new StringBuffer();
        for (String name : noticeUser) {
            stringBuffer.append("@").append(name).append(" ");
        }
        content = stringBuffer + "\r\n" + content;
        System.out.println(content);
    }
    public static void main(String[] args) {
        test();
        //String dateString = "2023-11-09 10:06:42";
        //String format = "yyyy-MM-dd HH:mm:ss";
        //Date date = convertStringToDate(dateString, format);
        //boolean before = date.before(new Date());
        //System.out.println(before);
    }
}
