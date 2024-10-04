package imooc.chapter2.part1;

import lombok.extern.slf4j.Slf4j;

import java.net.Socket;

/**
 * @Author lnd
 * @Description
 * @Date 2024/10/3 23:03
 */
@Slf4j
public class Client {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;
    private static final int SLEEP_TIME = 5000;

    public static void main(String[] args) throws Exception {
        final Socket socket = new Socket(HOST, PORT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("客户端启动成功");
                while (true) {
                    String msg = "hello world";
                    log.info("客户端发送数据：{}", msg);
                    try {
                        socket.getOutputStream().write(msg.getBytes());
                    } catch (Exception e) {
                        log.error("写数据出错", e);
                    }
                    sleep();
                }
            }
        }).start();
    }

    private static void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
