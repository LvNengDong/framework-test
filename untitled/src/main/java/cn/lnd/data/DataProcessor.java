package cn.lnd.data;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * @Author lnd
 * @Description
 * @Date 2023/4/11 19:15
 */
public class DataProcessor {

    public static void main(String[] args) {
        CharSource charSource = Files.asCharSource(new File("/Users/workspace/framework-test/untitled/src/main/resources/data/kfc.txt"), Charsets.UTF_8);
        CharSink charSink = Files.asCharSink(new File("/Users/workspace/framework-test/untitled/src/main/resources/data/kfcOver.txt"), Charsets.UTF_8);
        StringBuffer stringBuffer = new StringBuffer();
        int count = 0;
        try {
            for (String line : charSource.readLines()) {
                stringBuffer.append("'").append(line).append("'").append(",").append("\n");
                count++;
            }
            charSink.write(stringBuffer.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(count);
    }
}
