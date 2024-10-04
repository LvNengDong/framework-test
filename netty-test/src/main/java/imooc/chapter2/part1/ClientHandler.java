package imooc.chapter2.part1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * @Author lnd
 * @Description
 * @Date 2024/10/3 22:50
 */
@Slf4j
public class ClientHandler {
    public static final int MAX_DATA_LEN = 1024;

    /* 客户端Socket连接 */
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * 处理客户端连接
     * */
    public void process() {
        log.info("新客户端接入");
        new Thread(new Runnable() {
            @Override
            public void run() {
                doStart();
            }
        }).start();
    }

    private void doStart() {
        try {
            InputStream inputStream = socket.getInputStream();
            while (true) {
                byte[] data = new byte[MAX_DATA_LEN];
                int len;
                while ((len = inputStream.read(data)) != -1) {
                    String msg = new String(data, 0, len);
                    log.info("客户端传来消息：{}", msg);
                    // 将客户端传来的消息原封不动的再传回给客户端
                    socket.getOutputStream().write(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
