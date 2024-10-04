package imooc.chapter2.part1;

/**
 * @Author lnd
 * @Description
 * @Date 2024/10/3 22:42
 */
public class ServerBoot {
    private static final int PORT = 8000;

    public static void main(String[] args) {
        Server server = new Server(PORT);
        server.start();
    }
}
