package lg.lesson._03;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/6 23:38
 */
@Slf4j
public class HttpServer {

    public static void main(String[] args) throws InterruptedException {
        new HttpServer().start(8088);
    }

    private void start(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("codec", new HttpServerCodec()) // Http编解码
                                .addLast("compressor", new HttpContentCompressor()) // HttpContent压缩
                                .addLast("aggregator", new HttpObjectAggregator(65536)) //HTTP消息聚合
                                .addLast("handler", new HttpServerHandler()); //自定义业务逻辑处理器
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind().sync();
        log.info("Http Server started, Listening on {}", port);
        future.channel().closeFuture().sync();
    }
}
