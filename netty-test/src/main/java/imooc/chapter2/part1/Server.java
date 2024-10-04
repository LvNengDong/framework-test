package imooc.chapter2.part1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author lnd
 * @Description
 * @Date 2024/10/3 22:43
 */
@Slf4j
public class Server {
    private ServerSocket serverSocket;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            log.info("服务端启动成功，端口：{}", port);
        } catch (IOException e) {
            log.info("服务端启动失败");
        }
    }

    public void start() {
        // 我们不希望创建 Server 的动作阻塞 ServerBoot 的主线程，
        // 所以这里使用一个新的线程来创建 Server，执行端口监听的操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                doStart();
            }
        }).start();
    }

    private void doStart() {
        while (true) {
            try {
                // 监听客户端连接，当有客户端连接到服务端时，获得这个Socket
                Socket client = serverSocket.accept();
                // 使用 ClientHandler 处理客户端连接
                new ClientHandler(client).process();
            } catch (IOException e) {
                log.info("服务端异常");
            }
        }
    }
}
