package cn.lnd.qunar;

import java.io.*;

/**
 * @Author lnd
 * @Description
 * @Date 2023/8/18 11:39
 */
public class TextUtils {

    public static void main(String[] args) {
        test();
    }
    /*
    * 文本处理，给每一行文本后面加个逗号
    * */
    public static void test(){
        String inputFilePath = "/Users/workspace/framework-test/untitled/src/main/resources/data/dalong.txt"; // 输入文件路径
        String outputFilePath = "/Users/workspace/framework-test/untitled/src/main/resources/data/dalong2.txt"; // 输出文件路径
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                //line += ","; // 在行末加上逗号
                writer.write(line);
                //writer.newLine(); // 换行
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
