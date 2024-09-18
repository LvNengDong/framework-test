package cn.lnd.io;


import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/23 01:49
 */
public class IOTest {
    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream cachedBytes = new ByteArrayOutputStream();
        InputStream inputStream = getInputStream();
        System.out.println("start: " + inputStream);
        //IOUtils.copy(inputStream, cachedBytes);
        //StreamUtils.copyToByteArray(inputStream);
        inputStream.reset();
        System.out.println("after: " + inputStream);
    }

    private static InputStream getInputStream() {
        String str = "Hello, World!";
        InputStream inputStream = new ByteArrayInputStream(str.getBytes());
        return inputStream;
    }
}
