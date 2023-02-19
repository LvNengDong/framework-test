package cn.lnd.tmp;

import java.io.InputStream;

/**
 * @Author lnd
 * @Description 读取配置文件到输入流中
 * @Date 2023/2/12 23:11
 */
public class Resources {
    public static InputStream getResourceAsStream(String path) {
        InputStream resourceAsStream = Resources.class.getClassLoader().getResourceAsStream(path);
        return resourceAsStream;
    }
}
